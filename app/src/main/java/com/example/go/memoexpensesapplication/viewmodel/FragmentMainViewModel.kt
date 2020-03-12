package com.example.go.memoexpensesapplication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.go.memoexpensesapplication.dispatcher.MainDispatcher
import com.example.go.memoexpensesapplication.model.Expense
import com.example.go.memoexpensesapplication.navigator.FragmentMainNavigator
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class FragmentMainViewModel @Inject constructor(
    dispatcher: MainDispatcher
) : ViewModel() {

    private var navigator: FragmentMainNavigator? = null

    val isCheckable: MutableLiveData<Boolean> = MutableLiveData(false)
    private var expenses: List<Expense> = emptyList()

    private val _updateExpenses = PublishProcessor.create<List<Expense>>()
    val updateExpenses: Flowable<List<Expense>> = _updateExpenses
    private val compositeDisposable = CompositeDisposable()

    init {
        dispatcher.onGetAllExpenses
            .map { action -> expenses = action.data; expenses }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(_updateExpenses)
        dispatcher.onAddExpense
            .map { action -> expenses = expenses + action.data; expenses }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(_updateExpenses)
        dispatcher.onEditExpense
            .map { action ->
                expenses = expenses - expenses.single { it.id == action.data.id }
                expenses = expenses + action.data
                expenses
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(_updateExpenses)
        dispatcher.onDeleteExpense
            .map { action -> expenses = expenses - action.data; expenses }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(_updateExpenses)
        dispatcher.onMoveToTagList
            .map { action -> action.data }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    navigator?.onTransitionTagList()
                        ?: throw RuntimeException("Navigator must be set")
                },
                { throw it }
            ).addTo(compositeDisposable)
        dispatcher.onToggleCheckable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                isCheckable.postValue(isCheckable.value?.not())
            }.addTo(compositeDisposable)
    }

    fun setNavigator(navigator: FragmentMainNavigator) {
        this.navigator = navigator
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}