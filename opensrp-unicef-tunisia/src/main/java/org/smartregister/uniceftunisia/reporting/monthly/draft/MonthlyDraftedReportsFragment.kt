package org.smartregister.uniceftunisia.reporting.monthly.draft

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_monthly_drafted_reports.*
import org.smartregister.uniceftunisia.R
import org.smartregister.uniceftunisia.domain.MonthlyTally
import org.smartregister.uniceftunisia.reporting.*
import org.smartregister.uniceftunisia.reporting.monthly.MonthlyReportsRepository
import org.smartregister.uniceftunisia.reporting.monthly.MonthlyReportsViewModel
import org.smartregister.uniceftunisia.reporting.monthly.indicator.ReportIndicatorsActivity
import org.smartregister.view.customcontrols.CustomFontTextView
import org.smartregister.view.customcontrols.FontVariant
import java.io.Serializable
import java.util.*

class MonthlyDraftedReportsFragment : Fragment(), AdapterView.OnItemClickListener, View.OnClickListener {

    private lateinit var alertDialog: AlertDialog
    private val monthlyReportsViewModel by activityViewModels<MonthlyReportsViewModel>
    { ViewModelUtil.createFor(MonthlyReportsViewModel(MonthlyReportsRepository())) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_monthly_drafted_reports, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        monthlyReportsViewModel.run {
            unDraftedMonths.observe(viewLifecycleOwner, { dates ->
                startNewReportButton.apply {
                    isEnabled = dates.isNotEmpty()
                    background = if (dates.isEmpty()) ContextCompat.getDrawable(context, R.drawable.vaccination_earlier_bg)
                    else ContextCompat.getDrawable(context, R.drawable.report_btn_bg)
                    setTextColor(if (dates.isEmpty()) ContextCompat.getColor(context, R.color.translucent_client_list_grey)
                    else ContextCompat.getColor(context, R.color.white))
                    setOnClickListener { showAvailableReportDatesDialog(dates) }
                }
            })

            draftedMonths.observe(viewLifecycleOwner, {
                if (it.isEmpty()) noDraftReportsLayout.visibility = View.VISIBLE
                else {
                    noDraftReportsLayout.visibility = View.GONE
                    draftedReportsRecyclerView.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(context)
                        adapter = DraftedReportsRecyclerAdapter(it, this@MonthlyDraftedReportsFragment)
                        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                    }
                }
            })

            draftedReportTallies.observe(viewLifecycleOwner, {
                val (yearMonth, monthlyTallies) = it
                launchReportIndicatorsActivity(yearMonth = yearMonth, monthlyTallies = monthlyTallies)
            })
        }
    }

    private fun launchReportIndicatorsActivity(showData: Boolean = false, yearMonth: String, monthlyTallies: List<MonthlyTally>) {
        startActivity(Intent(activity, ReportIndicatorsActivity::class.java).apply {
            putExtras(Bundle().apply {
                putExtra(MONTHLY_TALLIES, monthlyTallies.associateBy { it.indicator } as Serializable)
                putExtra(YEAR_MONTH, yearMonth)
                putExtra(SHOW_DATA, showData)
            })
        })
    }

    private fun showAvailableReportDatesDialog(dates: List<String>) {
        LayoutInflater.from(context).inflate(R.layout.month_results, null).run {
            val baseAdapter: BaseAdapter = object : BaseAdapter() {
                override fun getCount() = dates.size

                override fun getItem(position: Int) = dates[position]

                override fun getItemId(position: Int) = position.toLong()

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val currentView = convertView
                            ?: LayoutInflater.from(context).inflate(R.layout.report_alert_dialog_item, parent, false)
                    return currentView.apply {
                        val yearMonth = getItem(position)
                        with(findViewById<TextView>(R.id.monthlyItemTextView)) {
                            text = yearMonth.translateString(requireContext())
                            tag = yearMonth
                        }
                        tag = yearMonth
                    }
                }
            }

            findViewById<ListView>(R.id.datesListView).apply {
                adapter = baseAdapter
                onItemClickListener = this@MonthlyDraftedReportsFragment
            }
            createAlertDialog(this)
        }

        alertDialog.show()
    }

    private fun createAlertDialog(view: View) {
        AlertDialog.Builder(requireActivity(), R.style.PathDialog).apply {
            setView(view)
            setCancelable(true)
            setCustomTitle(CustomFontTextView(activity).apply {
                text = getString(R.string.reports_available)
                gravity = Gravity.START
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                setFontVariant(FontVariant.BOLD)
                setPadding(
                        resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                        resources.getDimensionPixelSize(R.dimen.activity_vertical_margin),
                        resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                        resources.getDimensionPixelSize(R.dimen.activity_vertical_margin))
            })
            alertDialog = create()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val yearMonth = view.tag as String
        if (view.tag is String && yearMonth.isNotEmpty()) {
            monthlyReportsViewModel.fetchMonthlyDraftedReportTallies(yearMonth.convertToNamedMonth())
            alertDialog.dismiss()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onClick(view: View) {
        if (view.tag is Pair<*, *>) {
            val (yearMonth) = view.tag as Pair<String, Date>
            monthlyReportsViewModel.fetchMonthlyDraftedReportTallies(yearMonth)
        }
    }
}