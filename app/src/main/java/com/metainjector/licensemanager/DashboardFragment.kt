package com.metainjector.licensemanager

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private val TAG = "DashboardFragment"

    private val packageColors: Map<String, Int> by lazy {
        mapOf(
            "Monthly" to Color.parseColor("#3498DB"),    // Blue
            "Yearly" to Color.parseColor("#2ECC71"),     // Green
            "Free Trial" to Color.parseColor("#F39C12"), // Orange
            "N/A" to Color.parseColor("#95A5A6")         // Gray for others
        )
    }

    // Listeners
    private var licenseListener: ListenerRegistration? = null
    private var salesListener: ListenerRegistration? = null

    // Views
    private var tvTotalUsers: TextView? = null
    private var tvActiveUsers: TextView? = null
    private var tvExpiredUsers: TextView? = null
    private var tvPendingUsers: TextView? = null
    private var tvLicensesSent: TextView? = null
    private var tvDisabledUsers: TextView? = null
    private var tvBugReports: TextView? = null
    private var tvSecurityLogs: TextView? = null
    private var tvTodayActivity: TextView? = null
    private var tvLifetimeActivity: TextView? = null
    private var tvGlobalStats: TextView? = null
    private var tvTotalSales: TextView? = null
    private var btnToggleSales: ImageButton? = null
    private lateinit var pieChartUsers: PieChart
    private lateinit var barChartSales: BarChart
    private lateinit var barChartMonthlyRevenue: BarChart
    private lateinit var layoutUserCountByPackage: LinearLayout
    private lateinit var spinnerYear: Spinner

    // Clickable CardViews
    private var cardTotalUsers: CardView? = null
    private var cardActiveUsers: CardView? = null
    private var cardExpiredUsers: CardView? = null
    private var cardPendingUsers: CardView? = null
    private var cardLicensesSent: CardView? = null
    private var cardDisabledUsers: CardView? = null
    private var cardBugReports: CardView? = null
    private var cardSecurityLogs: CardView? = null
    private var cardTodayActivity: CardView? = null
    private var cardLifetimeActivity: CardView? = null
    private var cardGlobalStats: CardView? = null
    private var cardTotalSales: CardView? = null

    // Data holders
    private var allLicenses: List<License> = emptyList()
    private var allSales: List<Map<String, Any>> = emptyList()

    private var isSalesVisible = false
    private var totalSalesValue = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        initializeViews(view)
        setupClickListeners()
        loadAndApplyCardColors()
        animateCardViews()

        btnToggleSales?.setOnClickListener {
            isSalesVisible = !isSalesVisible
            updateSalesVisibility()
        }
    }

    override fun onStart() {
        super.onStart()
        setupDashboardListeners()
    }

    override fun onStop() {
        super.onStop()
        licenseListener?.remove()
        salesListener?.remove()
    }

    private fun initializeViews(view: View) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers)
        tvExpiredUsers = view.findViewById(R.id.tvExpiredUsers)
        tvPendingUsers = view.findViewById(R.id.tvPendingUsers)
        tvLicensesSent = view.findViewById(R.id.tvLicensesSent)
        tvDisabledUsers = view.findViewById(R.id.tvDisabledUsers)
        tvBugReports = view.findViewById(R.id.tvBugReports)
        tvSecurityLogs = view.findViewById(R.id.tvSecurityLogs)
        tvTodayActivity = view.findViewById(R.id.tvTodayActivity)
        tvLifetimeActivity = view.findViewById(R.id.tvLifetimeActivity)
        tvGlobalStats = view.findViewById(R.id.tvGlobalStats)
        tvTotalSales = view.findViewById(R.id.tvTotalSales)
        btnToggleSales = view.findViewById(R.id.btnToggleSales)
        pieChartUsers = view.findViewById(R.id.pieChartUsers)
        barChartSales = view.findViewById(R.id.barChartSales)
        barChartMonthlyRevenue = view.findViewById(R.id.barChartMonthlyRevenue)
        layoutUserCountByPackage = view.findViewById(R.id.layoutUserCountByPackage)
        spinnerYear = view.findViewById(R.id.spinnerYear)

        cardTotalUsers = view.findViewById(R.id.cardTotalUsers)
        cardActiveUsers = view.findViewById(R.id.cardActiveUsers)
        cardExpiredUsers = view.findViewById(R.id.cardExpiredUsers)
        cardPendingUsers = view.findViewById(R.id.cardPendingUsers)
        cardLicensesSent = view.findViewById(R.id.cardLicensesSent)
        cardDisabledUsers = view.findViewById(R.id.cardDisabledUsers)
        cardBugReports = view.findViewById(R.id.cardBugReports)
        cardSecurityLogs = view.findViewById(R.id.cardSecurityLogs)
        cardTodayActivity = view.findViewById(R.id.cardTodayActivity)
        cardLifetimeActivity = view.findViewById(R.id.cardLifetimeActivity)
        cardGlobalStats = view.findViewById(R.id.cardGlobalStats)
        cardTotalSales = view.findViewById(R.id.cardTotalSales)
    }

    private fun setupClickListeners() {
        cardTotalUsers?.setOnClickListener { openFilteredList("Total") }
        cardActiveUsers?.setOnClickListener { openFilteredList("Active") }
        cardExpiredUsers?.setOnClickListener { openFilteredList("Expired") }
        cardPendingUsers?.setOnClickListener { openFilteredList("Pending") }
        cardLicensesSent?.setOnClickListener { openFilteredList("Sent") }
        cardDisabledUsers?.setOnClickListener { openFilteredList("Disabled") }
    }

    private fun openFilteredList(filterType: String) {
        val intent = Intent(activity, FilteredUserListActivity::class.java)
        intent.putExtra("FILTER_TYPE", filterType)
        startActivity(intent)
    }

    private fun animateCardViews() {
        val cards = listOfNotNull(
            cardTotalSales,
            cardTotalUsers, cardActiveUsers, cardExpiredUsers,
            cardPendingUsers, cardLicensesSent, cardDisabledUsers,
            cardBugReports, cardSecurityLogs, cardTodayActivity,
            cardLifetimeActivity, cardGlobalStats
        )

        for (i in cards.indices) {
            val card = cards[i]
            card.alpha = 0f
            card.translationY = 100f

            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator(1.5f))
                .setDuration(500)
                .setStartDelay((i * 60).toLong())
                .start()
        }
    }

    private fun loadAndApplyCardColors() {
        if (!isAdded) return
        val sharedPreferences = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        cardTotalUsers?.setCardBackgroundColor(sharedPreferences.getInt("color_total_users", Color.parseColor("#007BFF")))
        cardActiveUsers?.setCardBackgroundColor(sharedPreferences.getInt("color_active_users", Color.parseColor("#28A745")))
        cardExpiredUsers?.setCardBackgroundColor(sharedPreferences.getInt("color_expired_users", Color.parseColor("#DC3545")))
        cardPendingUsers?.setCardBackgroundColor(sharedPreferences.getInt("color_pending_users", Color.parseColor("#FFC107")))
        cardLicensesSent?.setCardBackgroundColor(sharedPreferences.getInt("color_licenses_sent", Color.parseColor("#6F42C1")))
        cardDisabledUsers?.setCardBackgroundColor(sharedPreferences.getInt("color_disabled_users", Color.parseColor("#6c757d")))
        cardBugReports?.setCardBackgroundColor(Color.parseColor("#17A2B8"))
        cardSecurityLogs?.setCardBackgroundColor(Color.parseColor("#343A40"))
        cardTodayActivity?.setCardBackgroundColor(Color.parseColor("#E83E8C"))
        cardLifetimeActivity?.setCardBackgroundColor(Color.parseColor("#6610F2"))
        cardGlobalStats?.setCardBackgroundColor(Color.parseColor("#20C997"))
        cardTotalSales?.setCardBackgroundColor(Color.parseColor("#FD7E14"))
    }

    private fun setupDashboardListeners() {
        licenseListener = db.collection("licenseDatabase")
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.w(TAG, "License listen failed.", e); return@addSnapshotListener }
                allLicenses = snapshots?.mapNotNull { doc -> doc.toObject(License::class.java).copy(id = doc.id) } ?: emptyList()
                updateLicenseDependentUI()
            }

        salesListener = db.collection("salesLogs")
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.w(TAG, "Sales listen failed.", e); return@addSnapshotListener }
                allSales = snapshots?.map { it.data } ?: emptyList()
                updateSalesDependentUI()
            }
        fetchOtherStats()
    }

    private fun updateLicenseDependentUI() {
        if (view == null) return
        val assignedLicenses = allLicenses.filter { !it.Email.isNullOrBlank() }

        tvTotalUsers?.text = assignedLicenses.size.toString()
        tvActiveUsers?.text = assignedLicenses.count { it.Status == "Active" }.toString()
        tvExpiredUsers?.text = assignedLicenses.count { it.Status == "Expired" }.toString()
        tvPendingUsers?.text = assignedLicenses.count { it.Status == "Pending" }.toString()
        tvLicensesSent?.text = assignedLicenses.count { it.Status == "Sent" }.toString()
        tvDisabledUsers?.text = assignedLicenses.count { it.Status == "Disabled" }.toString()

        val packageCounts = assignedLicenses.groupingBy { it.Package ?: "N/A" }.eachCount()

        if (packageCounts.isEmpty()) {
            pieChartUsers.clear()
            barChartSales.clear()
            layoutUserCountByPackage.removeViews(1, layoutUserCountByPackage.childCount - 1)
        } else {
            setupPieChart(packageCounts)
            setupBarChartSales(packageCounts)
            updateUserCountTable(packageCounts)
        }
    }

    private fun updateSalesDependentUI() {
        if (view == null) return
        totalSalesValue = 0.0
        allSales.forEach { sale ->
            val price = when (val p = sale["Final Price"]) {
                is Number -> p.toDouble()
                is String -> p.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            totalSalesValue += price
        }
        updateSalesVisibility()
        setupYearSpinner()
    }

    private fun updateSalesVisibility() {
        if (isSalesVisible) {
            val formattedPrice = "৳${"%,.0f".format(totalSalesValue)}"
            tvTotalSales?.text = formattedPrice
            btnToggleSales?.setImageResource(R.drawable.ic_eye_slashed)
        } else {
            tvTotalSales?.text = "****"
            btnToggleSales?.setImageResource(R.drawable.ic_eye)
        }
    }

    private fun fetchOtherStats() {
        db.collection("bugReports").get().addOnSuccessListener {
            if (view == null) return@addOnSuccessListener
            tvBugReports?.text = it.size().toString()
        }
        db.collection("securityLogs").get().addOnSuccessListener {
            if (view == null) return@addOnSuccessListener
            tvSecurityLogs?.text = it.size().toString()
        }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.collection("dailyUsage").document(todayStr).get().addOnSuccessListener { doc ->
            if (view == null) return@addOnSuccessListener
            tvTodayActivity?.text = doc.getLong("count")?.toString() ?: "0"
        }
        db.collection("lifetimeUsage").document("main").get().addOnSuccessListener { doc ->
            if (view == null) return@addOnSuccessListener
            tvLifetimeActivity?.text = doc.getLong("count")?.toString() ?: "0"
        }
        db.collection("GlobalStats").document("main").get().addOnSuccessListener { doc ->
            if (view == null) return@addOnSuccessListener
            tvGlobalStats?.text = doc.getLong("totalFilesProcessed")?.toString() ?: "0"
        }
    }

    private fun setupPieChart(packageCounts: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        packageCounts.forEach { (pkg, count) ->
            entries.add(PieEntry(count.toFloat(), pkg))
            colors.add(packageColors[pkg] ?: Color.LTGRAY)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueFormatter = IntValueFormatter()

        pieChartUsers.data = PieData(dataSet)
        pieChartUsers.description.isEnabled = false
        pieChartUsers.legend.isEnabled = false
        pieChartUsers.isDrawHoleEnabled = true
        pieChartUsers.setHoleColor(Color.TRANSPARENT)
        pieChartUsers.setTransparentCircleColor(Color.TRANSPARENT)
        pieChartUsers.setEntryLabelColor(Color.WHITE)
        pieChartUsers.setEntryLabelTextSize(12f)
        pieChartUsers.animateY(1200)
        pieChartUsers.invalidate()
    }

    private fun setupBarChartSales(packageCounts: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val colors = ArrayList<Int>()
        var i = 0f
        packageCounts.forEach { (pkg, count) ->
            entries.add(BarEntry(i, count.toFloat()))
            labels.add(pkg)
            colors.add(packageColors[pkg] ?: Color.LTGRAY)
            i++
        }

        val dataSet = BarDataSet(entries, "Sales Count")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = IntValueFormatter()

        barChartSales.data = BarData(dataSet)
        barChartSales.description.isEnabled = false
        barChartSales.legend.isEnabled = false
        barChartSales.setFitBars(true)
        barChartSales.animateY(1200)

        val xAxis = barChartSales.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        barChartSales.axisRight.isEnabled = false
        barChartSales.invalidate()
    }

    private fun updateUserCountTable(packageCounts: Map<String, Int>) {
        if (context == null) return
        layoutUserCountByPackage.removeViews(1, layoutUserCountByPackage.childCount - 1)
        val sortedData = packageCounts.entries.sortedByDescending { it.value }

        sortedData.forEachIndexed { index, entry ->
            val row = LayoutInflater.from(context).inflate(R.layout.list_item_package_count, layoutUserCountByPackage, false)
            val colorView = row.findViewById<View>(R.id.packageColorView)
            val tvPackageName = row.findViewById<TextView>(R.id.tvPackageName)
            val tvPackageCount = row.findViewById<TextView>(R.id.tvPackageCount)

            colorView.setBackgroundColor(packageColors[entry.key] ?: Color.LTGRAY)
            tvPackageName.text = entry.key
            tvPackageCount.text = entry.value.toString()

            layoutUserCountByPackage.addView(row)
        }
    }

    private fun setupYearSpinner() {
        val years = allSales.mapNotNull {
            (it["Timestamp"] as? Timestamp)?.toDate()?.let { date ->
                SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
            }
        }.distinct().sortedDescending()

        if (context == null || years.isEmpty()) {
            spinnerYear.visibility = View.GONE
            return
        }

        spinnerYear.visibility = View.VISIBLE
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = adapter
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    val selectedYear = years[position].toInt()
                    setupMonthlyRevenueChart(selectedYear)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing year selection.", e)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupMonthlyRevenueChart(year: Int) {
        val monthlyTotals = DoubleArray(12) { 0.0 }
        val monthLabels = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val calendar = Calendar.getInstance()

        allSales.forEach { sale ->
            (sale["Timestamp"] as? Timestamp)?.toDate()?.let { date ->
                calendar.time = date
                if (calendar.get(Calendar.YEAR) == year) {
                    val month = calendar.get(Calendar.MONTH) // 0-11
                    val price = when (val p = sale["Final Price"]) {
                        is Number -> p.toDouble()
                        is String -> p.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    monthlyTotals[month] += price
                }
            }
        }

        val entries = ArrayList<BarEntry>()
        for (i in monthlyTotals.indices) {
            entries.add(BarEntry(i.toFloat(), monthlyTotals[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "Revenue in BDT")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueFormatter = IntValueFormatter()

        barChartMonthlyRevenue.data = BarData(dataSet)
        barChartMonthlyRevenue.description.isEnabled = false
        barChartMonthlyRevenue.legend.isEnabled = false
        barChartMonthlyRevenue.setFitBars(true)
        barChartMonthlyRevenue.animateY(1200)

        val xAxis = barChartMonthlyRevenue.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        barChartMonthlyRevenue.axisRight.isEnabled = false
        barChartMonthlyRevenue.invalidate()
    }
}