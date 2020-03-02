package com.example.go.memoexpensesapplication.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.go.memoexpensesapplication.Prefs
import com.example.go.memoexpensesapplication.R
import com.example.go.memoexpensesapplication.action.MainAction
import com.example.go.memoexpensesapplication.databinding.DialogViewFragmentMainAddBinding
import com.example.go.memoexpensesapplication.databinding.FragmentExpenseListBinding
import com.example.go.memoexpensesapplication.model.Expense
import com.example.go.memoexpensesapplication.model.User
import com.example.go.memoexpensesapplication.view.adapter.ExpenseListAdapter
import com.example.go.memoexpensesapplication.view.adapter.TagListSpinnerAdapter
import com.example.go.memoexpensesapplication.viewmodel.MainFragmentViewModel

class ExpenseListFragment : Fragment(), ExpenseListAdapter.OnClickExpenseListener {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var expenseListAdapter: ExpenseListAdapter

    private lateinit var viewModel: MainFragmentViewModel
    private lateinit var binding: FragmentExpenseListBinding

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setTitle(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[MainFragmentViewModel::class.java]
        expenseListAdapter =
            ExpenseListAdapter(viewModel.data.value.orEmpty(), this@ExpenseListFragment).apply {
                setHeader()
                setFooter()
            }

        binding.expenseList.apply {
            adapter = expenseListAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.data.observe(this, Observer {
            expenseListAdapter.update(it)
        })

        binding.buttonAddExpense.setOnClickListener {
            val binding =
                DialogViewFragmentMainAddBinding.inflate(layoutInflater, view as ViewGroup, false)
            val tags = Prefs.getTags().toList()
            binding.tag.adapter = TagListSpinnerAdapter(context!!, tags)

            val builder = context?.let {
                AlertDialog.Builder(it)
                    .setTitle(R.string.fragment_main_add_title)
                    .setView(binding.root)
                    .setPositiveButton(R.string.add) { _, _ ->
                        val item = Expense(
                            user.uid,
                            binding.tag.selectedItem as String,
                            binding.value.text.toString().toInt(10),
                            binding.memo.text.toString()
                        )
                        viewModel.send(MainAction.AddExpense(item))
                    }
                    .setNegativeButton(R.string.cancel, null)
            } ?: return@setOnClickListener
            MyDialogFragment().setBuilder(builder)
                .show((activity as AppCompatActivity).supportFragmentManager, null)
        }

        viewModel.send(MainAction.GetExpense(user.uid))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_fragment_main_edit_tag -> {
                listener?.onTransitionTagList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickExpense(v: View, position: Int, item: Expense) {
        val builder = context?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.fragment_main_remove_title)
                .setMessage(getString(R.string.fragment_main_remove_message, item.tag, item.value))
                .setPositiveButton(R.string.ok) { _, _ ->
                    viewModel.send(MainAction.DeleteExpense(item))
                }
                .setNegativeButton(R.string.cancel, null)
        } ?: return
        MyDialogFragment().setBuilder(builder)
            .show((activity as AppCompatActivity).supportFragmentManager, null)
    }

    fun setUser(user: User) {
        this.user = user
    }

    interface OnFragmentInteractionListener {
        fun onTransitionTagList()
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User) = ExpenseListFragment().apply {
            setUser(user)
        }
    }
}