package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SplitDatabase
import com.example.data.SplitRecord
import com.example.data.SplitRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale

class SplitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SplitRepository
    private val prefs = application.getSharedPreferences("spliky_settings", Context.MODE_PRIVATE)

    init {
        val database = SplitDatabase.getDatabase(application)
        repository = SplitRepository(database.splitDao())
    }

    // Currency Setting
    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "₹") ?: "₹")
    val currencySymbol = _currencySymbol.asStateFlow()

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
        prefs.edit().putString("currency_symbol", symbol).apply()
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(AppTab.SCAN)
    val currentTab = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Scanning State
    private val _scannedImageUri = MutableStateFlow<Uri?>(null)
    val scannedImageUri = _scannedImageUri.asStateFlow()

    private val _scanState = MutableStateFlow<ScanProcessState>(ScanProcessState.Idle)
    val scanState = _scanState.asStateFlow()

    // Detected receipt items
    private val _detectedItems = MutableStateFlow<List<ReceiptItem>>(emptyList())
    val detectedItems = _detectedItems.asStateFlow()

    // Extracted Total Amount
    private val _extractedTotal = MutableStateFlow(0.0)
    val extractedTotal = _extractedTotal.asStateFlow()

    // Manual total option
    private val _manualTotal = MutableStateFlow("")
    val manualTotal = _manualTotal.asStateFlow()

    // Active subtotal for split (derived or manual)
    val activeSubtotal: Double
        get() {
            val manualVal = _manualTotal.value.toDoubleOrNull()
            if (manualVal != null && manualVal >= 0.0) {
                return manualVal
            }
            return if (_extractedTotal.value > 0.0) _extractedTotal.value else 0.0
        }

    // People count (2..20)
    private val _peopleCount = MutableStateFlow(2)
    val peopleCount = _peopleCount.asStateFlow()

    // Tip percentage (0, 10, 15, 18, custom)
    private val _tipPercent = MutableStateFlow(10.0)
    val tipPercent = _tipPercent.asStateFlow()

    private val _customTipText = MutableStateFlow("")
    val customTipText = _customTipText.asStateFlow()

    // Setters
    fun setScannedImageUri(uri: Uri?) {
        _scannedImageUri.value = uri
    }

    fun setManualTotal(value: String) {
        _manualTotal.value = value
    }

    fun resetScanner() {
        _scannedImageUri.value = null
        _scanState.value = ScanProcessState.Idle
        _detectedItems.value = emptyList()
        _extractedTotal.value = 0.0
        _manualTotal.value = ""
    }

    fun incrementPeople() {
        if (_peopleCount.value < 20) {
            _peopleCount.value += 1
        }
    }

    fun decrementPeople() {
        if (_peopleCount.value > 1) {
            _peopleCount.value -= 1
        }
    }

    fun setPeopleCount(count: Int) {
        if (count in 1..20) {
            _peopleCount.value = count
        }
    }

    fun setTipPercent(percent: Double) {
        _tipPercent.value = percent
        _customTipText.value = ""
    }

    fun setCustomTip(text: String) {
        _customTipText.value = text
        val p = text.toDoubleOrNull()
        if (p != null && p >= 0.0) {
            _tipPercent.value = p
        } else {
            _tipPercent.value = 0.0
        }
    }

    // OCR function
    fun performTextRecognition(context: Context, uri: Uri) {
        _scanState.value = ScanProcessState.Processing
        _scannedImageUri.value = uri

        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    processOcrText(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e("SplitViewModel", "OCR Failed", e)
                    _scanState.value = ScanProcessState.Error("OCR processing failed: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("SplitViewModel", "Image Loading Failed", e)
            _scanState.value = ScanProcessState.Error("Failed to load image: ${e.message}")
        }
    }

    private fun processOcrText(text: String) {
        val lines = text.split("\n")
        val items = mutableListOf<ReceiptItem>()
        var foundTotal = 0.0

        val priceRegex = Regex("""(?:₹|Rs\.?|INR\s*)?\s*(\d{2,6}(?:\.\d{1,2})?)\s*$""")
        val dateRegex = Regex("""\d{2}[/\-]\d{2}[/\-]\d{2,4}""")
        val phoneRegex = Regex("""\d{10,}""")
        val gstRegex = Regex("""[a-zA-Z]{2}\d{2}[a-zA-Z]{3}\d+""")
        val timeRegex = Regex("""\d{1,2}:\d{2}""")
        
        val skipKeywords = listOf(
            "gst", "sgst", "cgst", "igst", "vat", "tax", "service charge", "service fee", 
            "discount", "offer", "coupon", "cash", "card", "upi", "gpay", "paytm", 
            "table", "cover", "date", "time", "bill no", "order", "kot", "phone", 
            "mobile", "address", "thank", "welcome", "receipt", "invoice", "server", 
            "waiter", "fssai", "gstin", "pan"
        )
        
        val primaryTotalKeywords = listOf(
            "grand total", "total amount", "net amount", "amount payable", 
            "total payable", "to pay", "total due"
        )

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            // 2. Reject these line patterns completely
            if (dateRegex.containsMatchIn(trimmedLine) || 
                phoneRegex.containsMatchIn(trimmedLine) || 
                gstRegex.containsMatchIn(trimmedLine) || 
                timeRegex.containsMatchIn(trimmedLine)) {
                continue
            }

            val lowerLine = trimmedLine.lowercase()
            
            // 3. Skip lines containing these keywords entirely
            if (skipKeywords.any { lowerLine.contains(it) }) {
                continue
            }

            // 1. Price must appear at the END of a line only
            val matchResult = priceRegex.find(trimmedLine)
            if (matchResult != null) {
                val priceStr = matchResult.groupValues[1]
                val price = priceStr.toDoubleOrNull() ?: continue

                // 4. Filter out invalid prices
                if (price < 10.0 || price > 100000.0) continue

                val label = trimmedLine.substring(0, matchResult.range.first).trim()
                    .replace(Regex("""^[^a-zA-Z0-9]+"""), "") // strip leading non-alphanumeric

                // Ignore lines where the name part is just a number
                if (label.trim().matches(Regex("""^\d+$"""))) continue

                val cleanLabel = if (label.isEmpty()) "Item ${items.size + 1}" else label
                items.add(ReceiptItem(name = cleanLabel, price = price))
            }
        }

        // 5. Total detection priority
        val primaryTotals = items.filter { item ->
            val l = item.name.lowercase()
            primaryTotalKeywords.any { l.contains(it) }
        }
        
        val fallbackTotals = items.filter { item ->
            val l = item.name.lowercase()
            l.contains("total") && !primaryTotals.contains(item)
        }

        if (primaryTotals.isNotEmpty()) {
            foundTotal = primaryTotals.maxOf { it.price }
        } else if (fallbackTotals.isNotEmpty()) {
            foundTotal = fallbackTotals.maxOf { it.price }
        } else {
            if (items.isNotEmpty()) {
                foundTotal = items.maxOf { it.price }
            }
        }

        val filteredItems = items.filter { it.price < foundTotal && !it.name.lowercase().contains("total") }
        val finalItems = if (filteredItems.isNotEmpty()) filteredItems else items

        // 6. Set manualTotal so the editable text field populates for correction
        _detectedItems.value = finalItems
        _extractedTotal.value = foundTotal
        _manualTotal.value = if (foundTotal > 0.0) String.format(Locale.US, "%.2f", foundTotal) else ""
        _scanState.value = ScanProcessState.Success(finalItems, foundTotal)
    }

    // Save to History Local Storage
    val allHistoryRecords: StateFlow<List<SplitRecord>> = repository.allSplits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveSplitRecord(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val sub = activeSubtotal
            val tipP = _tipPercent.value
            val pCount = _peopleCount.value
            val grand = sub + (sub * tipP / 100.0)
            val perP = if (pCount > 0) grand / pCount else grand

            val itemsList = _detectedItems.value.map { "${it.name}: ₹${String.format(Locale.US, "%.2f", it.price)}" }
            val itemsJson = JSONArray(itemsList).toString()

            val record = SplitRecord(
                subtotal = sub,
                tipPercent = tipP,
                peopleCount = pCount,
                perPersonAmount = perP,
                grandTotal = grand,
                itemsJson = itemsJson
            )

            repository.insert(record)
            onComplete()
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

enum class AppTab {
    SCAN, SPLIT, HISTORY, SETTINGS
}

sealed class ScanProcessState {
    object Idle : ScanProcessState()
    object Processing : ScanProcessState()
    data class Success(val items: List<ReceiptItem>, val total: Double) : ScanProcessState()
    data class Error(val message: String) : ScanProcessState()
}

data class ReceiptItem(
    val name: String,
    val price: Double
)
