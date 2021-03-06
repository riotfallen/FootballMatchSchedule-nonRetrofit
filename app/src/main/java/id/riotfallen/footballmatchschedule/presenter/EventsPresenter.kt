package id.riotfallen.footballmatchschedule.presenter

import com.google.gson.Gson
import id.riotfallen.footballmatchschedule.api.ApiRepository
import id.riotfallen.footballmatchschedule.api.TheSportDBApi
import id.riotfallen.footballmatchschedule.model.event.EventResponse
import id.riotfallen.footballmatchschedule.utils.CoroutineContextProvider
import id.riotfallen.footballmatchschedule.view.EventView
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

class EventsPresenter(private val view: EventView,
                      private val apiRepository: ApiRepository,
                      private val gson: Gson,
                      private val contextPool: CoroutineContextProvider = CoroutineContextProvider()) {

    fun getNextEventList(leagueId: String?){
        view.showLoading()

        async(contextPool.main) {
            val data = bg {
                gson.fromJson(apiRepository
                        .doRequest(TheSportDBApi.getNextMatch(leagueId)),
                        EventResponse::class.java
                )
            }
            view.showEvent(data.await().events)
            view.hideLoading()
        }
    }

    fun getPrevEventList(leagueId: String?){
        view.showLoading()

        async(contextPool.main) {
            val data = bg {
                gson.fromJson(apiRepository
                        .doRequest(TheSportDBApi.getPrevMatch(leagueId)),
                        EventResponse::class.java
                )
            }

            view.showEvent(data.await().events)
            view.hideLoading()
        }
    }

}