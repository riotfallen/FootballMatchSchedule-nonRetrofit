package id.riotfallen.footballmatchschedule.activity

import android.database.sqlite.SQLiteConstraintException
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import id.riotfallen.footballmatchschedule.R
import id.riotfallen.footballmatchschedule.api.ApiRepository
import id.riotfallen.footballmatchschedule.db.database
import id.riotfallen.footballmatchschedule.model.event.Event
import id.riotfallen.footballmatchschedule.model.favorite.Favorite
import id.riotfallen.footballmatchschedule.presenter.EventDetailPresenter
import id.riotfallen.footballmatchschedule.utils.BadgeFetcher
import id.riotfallen.footballmatchschedule.utils.invisible
import id.riotfallen.footballmatchschedule.utils.visible
import id.riotfallen.footballmatchschedule.view.EventView
import kotlinx.android.synthetic.main.activity_event_detail.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class EventDetailActivity : AppCompatActivity(), EventView {

    private lateinit var event: Event

    private lateinit var presenter: EventDetailPresenter
    private lateinit var idEvent: String
    private lateinit var idAway: String
    private lateinit var idHome: String

    private var menuItem: Menu? = null
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        idEvent = intent.getStringExtra("idEvent")
        idAway = intent.getStringExtra("idAway")
        idHome = intent.getStringExtra("idHome")

        setSupportActionBar(toolbarEventDetail)
        supportActionBar?.title = "Event Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val response = ApiRepository()
        val gson = Gson()

        presenter = EventDetailPresenter(this, response, gson)
        presenter.getEventDetail(idEvent)

        BadgeFetcher().loadBadges(idHome, imageViewBadgeHome)
        BadgeFetcher().loadBadges(idAway, imageViewBadgeAway)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail_event, menu)
        menuItem = menu

        favoriteState()
        setFavorite()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item != null) {
            when(item.itemId){
                android.R.id.home -> onBackPressed()
                R.id.add_to_favorite -> {
                    if(isFavorite) removeFromFavorite() else addToFavorite()

                    isFavorite =! isFavorite
                    setFavorite()
                }
            }
            true
        } else {
            false
        }
    }

    private fun setFavorite() {
        if(isFavorite)
            menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_added_to_favorites)
        else
            menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_add_to_favorites)
    }

    private fun removeFromFavorite() {
        try {
            database.use {
                delete(Favorite.TABLE_FAVORITE,
                        "(EVENT_ID = {id})",
                        "id" to idEvent)
            }
            toast("Removed from favorites")
        } catch (e : SQLiteConstraintException) {
            toast(e.localizedMessage)
        }
    }

    private fun addToFavorite() {
        try {
            database.use {
                insert(Favorite.TABLE_FAVORITE,
                        Favorite.EVENT_ID to idEvent,
                        Favorite.EVENT_DATE to event.dateEvent,
                        Favorite.HOME_ID to idHome,
                        Favorite.HOME_NAME to event.strHomeTeam,
                        Favorite.HOME_SCORE to event.intHomeScore,
                        Favorite.AWAY_ID to idAway,
                        Favorite.AWAY_NAME to event.strAwayTeam,
                        Favorite.AWAY_SCORE to event.intAwayScore
                )
            }
            toast("Added to Favorites")
        } catch (e : SQLiteConstraintException){
            toast(e.localizedMessage)
        }
    }

    private fun favoriteState(){
        database.use {
            val result = select(Favorite.TABLE_FAVORITE)
                    .whereArgs("(EVENT_ID = {id})",
                            "id" to idEvent)
            val favorite = result.parseList(classParser<Favorite>())
            if(!favorite.isEmpty()) isFavorite = true
        }
    }

    override fun showLoading() {
        progressbarEventDetail.visible()
        scrollViewEventDetail.invisible()
    }

    override fun hideLoading() {
        progressbarEventDetail.invisible()
        scrollViewEventDetail.visible()
    }

    override fun showEvent(data: List<Event>) {
        event = data[0]

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(event.dateEvent)
        val dateText = SimpleDateFormat("EEEE, dd-MM-yyyy", Locale.getDefault())
                .format(date).toString()

        textViewEventDate.text = dateText

        textViewHomeGoal.text = event.strHomeGoalDetails?.replace(";", "\n")
        textViewAwayGoal.text = event.strAwayGoalDetails?.replace(";", "\n")

        if(event.intHomeScore != null) textViewHomeScore.text = event.intHomeScore.toString()
        if(event.intAwayScore != null)textViewAwayScore.text = event.intAwayScore.toString()

        if(event.intHomeShots != null) TextViewShotHome.text = event.intHomeShots.toString()
        if(event.intAwayShots != null) TextViewShotAway.text = event.intAwayShots.toString()

        TextViewGKHome.text = event.strHomeLineupGoalkeeper
        TextViewGKAway.text = event.strAwayLineupGoalkeeper

        TextViewDefenseHome.text = event.strHomeLineupDefense?.replace("; ", "\n")
        TextViewDefenseAway.text = event.strAwayLineupDefense?.replace("; ", "\n")

        TextViewMidFieldHome.text = event.strHomeLineupMidfield?.replace("; ", "\n")
        TextViewMidFieldAway.text = event.strAwayLineupMidfield?.replace("; ", "\n")

        TextViewForwardHome.text = event.strHomeLineupForward?.replace("; ", "\n")
        TextViewForwardAway.text = event.strAwayLineupForward?.replace("; ", "\n")

        TextViewSubstitutesHome.text = event.strHomeLineupSubstitutes?.replace("; ", "\n")
        TextViewSubstitutesAway.text = event.strAwayLineupSubstitutes?.replace("; ", "\n")
    }
}
