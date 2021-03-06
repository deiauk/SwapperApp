package app.swapper.com.swapper.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.support.v7.widget.RecyclerView
import android.util.Log
import app.swapper.com.swapper.adapter.DataPresenterInterface
import app.swapper.com.swapper.dto.User
import app.swapper.com.swapper.dto.UserData
import app.swapper.com.swapper.events.OnUserDataLoaded
import app.swapper.com.swapper.networking.ApiService
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Deividas on 2018-05-01.
 */
abstract class RecyclerViewViewModel<out T : RecyclerView.Adapter<*>>(private val service: ApiService?, private val user: User?): ViewModel() {

    abstract fun getAdapter(): T

    fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = getAdapter()
    }

    fun askServerForUserItems() {
        user?.let {
            val call = service?.getUserItems(it.email)
            call?.enqueue(object : Callback<UserData> {
                override fun onResponse(call: Call<UserData>?, response: Response<UserData>?) {
                    response?.let {
                        if (response.isSuccessful) {
                            EventBus.getDefault().post(OnUserDataLoaded())
                            val userItems = response.body()
                            userItems?.let {
                                (getAdapter() as DataPresenterInterface).setDataList(it.items)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<UserData>?, t: Throwable?) {
                    Log.d("ASDSUIADSDS", t?.message)
                }
            })
        }
    }
}