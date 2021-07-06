package com.team15.skredet

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.team15.skredet.dataclasses.AllRegionsDetailsData
import com.team15.skredet.dataclasses.PolygonData
import com.team15.skredet.dataclasses.RegionBasicData
import com.team15.skredet.externalFiles.ExternalFileHandler
import com.team15.skredet.externalFiles.StoredUserData
import com.team15.skredet.notif.BackgroundWorker
import com.team15.skredet.notif.NotificationHelper
import com.team15.skredet.ui.NoWifiFragment
import com.team15.skredet.ui.helpfrag.HelpFragment
import com.team15.skredet.ui.SettingsFragment
import com.team15.skredet.ui.map.MapFragment
import com.team15.skredet.ui.region.FavoritesFragment
import com.team15.skredet.ui.region.RegionDetailFragment
import com.team15.skredet.ui.region.RegionsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Starts all fragments and organizes datacollection and navigation drawer
 * @author Permki & Haakose
 */
open class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var currFrag: Fragment
    private lateinit var noWifiFrag: Fragment
    private lateinit var vm: SharedViewModel
    private lateinit var toggle: ActionBarDrawerToggle
    private val tag = "MainActivity"

    /**
     * Extensions to Fragment. Helps show and hide frags in the host-frag
     * @author Permki
     */
    private fun Fragment.add() = supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment, this).commit()
    private fun Fragment.show() = supportFragmentManager.beginTransaction().show(this).commit()
    private fun Fragment.hide() = supportFragmentManager.beginTransaction().hide(this).commit()

    /**
     * Creates main by setting up nav_drawer, checking userdata and initializing all listeners and fragments
     * @author Permki
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initializeListeners()
        getData()
        createFragments()
        deleteUserData()
        fetchUserData()
        if (savedInstanceState == null) { startFirstFrag() }
    }

    private fun startFirstFrag(){
        if (vm.showWelcomePage) {
            vm.fragStack.push(vm.fragmentList[vm.startFragId]!!)
            vm.startFragId = R.id.action_help
            supportActionBar?.hide()
        }
        currFrag = vm.fragmentList[vm.startFragId]!!
        supportActionBar?.title = vm.fragmentTitles[vm.startFragId]
        currFrag.show()
        checkNetWork()
    }

    /**
     * Inflates options menu
     * @author Permki
     * @param menu From resources
     * @return Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * Handles backpress.
     * If drawer is open, close it.
     * If in settingsmenu or details, restore standard setup for toolbar and return to previous fragment
     * @author Permki
     */
    override fun onBackPressed() {
        if (noWifiFrag.isVisible) {
            super.onBackPressed()
        } else {
            when(currFrag){
                vm.fragmentList[R.id.nav_settings] -> {
                    closeWhenBackPress()
                    updateSettingsValues()
                }
                vm.fragmentList[R.id.action_help] -> {
                    closeWhenBackPress()

                    supportActionBar?.show()
                }
                vm.fragmentList[R.id.mainText] -> closeWhenBackPress()
                else -> {
                    if (drawer_layout.isDrawerVisible(GravityCompat.START)) { drawer_layout.closeDrawer(GravityCompat.START)}
                    if (vm.fragStack.size > 0){
                        currFrag.hide()
                        currFrag = vm.fragStack.pop()
                        currFrag.show()
                    } else {
                        super.onBackPressed()
                    }
                }
            }
        }


    }

    /**
     * Checks registered settings-values and forwards all info
     * @author Permki
     */
    private fun updateSettingsValues(){

        vm.mapType.value = vm.mapTypePicker[maptypeSpinner.selectedItem.toString()]!!
        when (animationspeedSpinner.selectedItem.toString()){
            "Sakte" -> vm.animationDelay = 500L
            "Vanlig" -> vm.animationDelay = 200L
            "Hurtig" -> vm.animationDelay = 100L
        }
        if (vm.tempNumDays > vm.biggestNum) {
            vm.biggestNum = vm.tempNumDays
            vm.numDays.value = vm.tempNumDays
            getData()
        } else {
            vm.numDays.value = vm.tempNumDays
            vm.updateSeekbarSize.value = true
        }
    }

    /**
     * Switching frags upon backbutton-press
     * @author Permki
     */
    private fun closeWhenBackPress(){
        currFrag.hide()
        currFrag = vm.fragStack.pop()
        currFrag.show()
        updateActionBarTitle(vm.fragmentList.filter{currFrag == it.value}.keys.first(), "")
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toggle.isDrawerIndicatorEnabled = true
        toggle.toolbarNavigationClickListener = null
    }

    /**
     * Initializes listeners and viewmodel-reference.
     * Pretty self-explanatory
     * @author Permki
     */
    private fun initializeListeners(){
        vm = ViewModelProvider(this)[SharedViewModel::class.java]
        vm.main = this
        vm.updatedTime.observe(this, Observer{ updated_info.text = vm.updatedTime.value })
        vm.updateDetails.observe(this, Observer{ showDetailsFrag() })
        vm.internet.observe(this, Observer{checkNetWork()})
        nav_view.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        vm.infoPageShown.observe(this, Observer {
            onBackPressed()
            vm.showWelcomePage = !vm.infoPageShown.value!! })
    }

    /**
     * Displays missing_network_frag if needed and attempts refresh and datacollection
     * If not needed, removes frag and resumes execution
     * @author Permki
     */
    private fun checkNetWork(){
        if (vm.internet.value == true){
            noWifiFrag.hide()
            currFrag.show()
            if (currFrag != vm.fragmentList[R.id.action_help]) supportActionBar?.show()

        } else {
            noWifiFrag.show()
            currFrag.hide()
            supportActionBar?.hide()
            when (vm.updateFailureOrigin){
                "Main" -> getData()
                "Details" -> vm.updateDetails.value = true
                "Map" -> vm.currentCoordinates.value = vm.currentCoordinates.value
            }
        }
    }

    /**
     * Hides hamburger, sets title with region-name and displays regiondetails
     * @author Permki
     */
    private fun showDetailsFrag(){
        removeHamburger()
        pushFrag()
        updateActionBarTitle(R.id.mainText, vm.currentRegionName.value!!)
        showFrag(R.id.mainText)
    }

    /**
     * Initializes all fragments and adds them and their titlenames to the lists used throughout the app.
     * @author Permki
     */
    private fun createFragments(){
        noWifiFrag = NoWifiFragment()
        vm.fragmentList[R.id.noWifi] = noWifiFrag
        vm.fragmentList[R.id.nav_my_areas] = FavoritesFragment()
        vm.fragmentList[R.id.nav_regions] = RegionsFragment()
        vm.fragmentList[R.id.nav_settings] = SettingsFragment()
        vm.fragmentList[R.id.mainText] = RegionDetailFragment()
        vm.fragmentList[R.id.action_help] = HelpFragment(vm)
        vm.fragmentList[R.id.nav_home] = MapFragment()
        vm.fragmentTitles[R.id.nav_my_areas] = getString( R.string.menu_critical)
        vm.fragmentTitles[R.id.nav_regions] = getString(R.string.menu_regions)
        vm.fragmentTitles[R.id.nav_settings] = getString(R.string.menu_settings)
        vm.fragmentTitles[R.id.mainText] = getString(R.string.menu_details)
        vm.fragmentTitles[R.id.action_help] = getString(R.string.action_help)
        vm.fragmentTitles[R.id.noWifi] = getString(R.string.nav_nowifi)
        vm.fragmentTitles[R.id.nav_home] = getString(R.string.menu_map)
        vm.noHamburgerFrags.add(R.id.action_settings)
        vm.noHamburgerFrags.add(R.id.action_help)
        vm.noHamburgerFrags.add(R.id.nav_settings)

        for (frag in vm.fragmentList.values){
            frag.add()
            frag.hide()
        }
        vm.fragmentTitles[R.id.action_settings] =  vm.fragmentTitles[R.id.nav_settings]!!
        vm.fragmentList[R.id.action_settings] = vm.fragmentList[R.id.nav_settings]!!
    }

    /**
     * Simply forwards the call to onNavigationItemSelected
     * @author Permki
     * @param item The menuitem selected
     * @return Boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean{return onNavigationItemSelected(item) }

    /**
     * Handles the nav_drawer on_selection.
     * Hides the current fragment and displays the next.
     * @author Permki
     * @param item The menuitem selected
     * @return Boolean
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_animate){
            if (!vm.animating) vm.animate.value = true
        } else {
            if (item.itemId == R.id.nav_update) {
                vm.colorMap.value = false
                getData()
                vm.colorMap.value = true
                vm.update.value = true
            } else {
                switchFrag(item.itemId)
            }
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    /**
     * Handles optionsmenu-selections by passing on to onNavigationItemSelected
     * @author Permki
     * @param item The element selected
     * @return Boolean
     */

    private fun updateActionBarTitle(itemID: Int, addon: String) {supportActionBar?.title = vm.fragmentTitles[itemID] + " " + addon}

    /**
    * Selfexplanatory
    * @author Permki
     * @param itemID Id used to retrieve frag
    */
    private fun switchFrag(itemID: Int){
        if (itemID in vm.noHamburgerFrags){ removeHamburger() }
        pushFrag()
        updateActionBarTitle(itemID, "")
        showFrag(itemID)
    }

    /**
     * Selfexplanatory
     * @author Permki
     */
    private fun pushFrag(){
        if (currFrag !in vm.fragStack) vm.fragStack.push(currFrag)
        currFrag.hide()
    }

    /**
     * Selfexplanatory
     * @author Permki
     * @param itemID Id used to retrieve frag
     */
    private fun showFrag(itemID: Int){
        currFrag = vm.fragmentList[itemID]!!
        currFrag.show()
    }

    /**
     * Replaces hamburger with back-arrow-button
     * @author Permki
     */
    private fun removeHamburger(){
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toggle.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.setToolbarNavigationClickListener { onBackPressed() }
    }

    /**
     * Collects all map-information for all regions. I.e. all coordinates used when drawing the polygons.
     * The information is stored in the list of RegionBasicData, but all info on polygons is then stored into
     * the list of polygons which is the list used in later operations.
     * @author Permki
     */
    private  fun getPolygonData() = MainScope().launch {
        mapProgressbar.show()
        try {
            val response = Fuel.get(getString(R.string.region_stamdata_url)).awaitString()
            vm.internet.value = true
            for (element in Gson().fromJson(response, Array<RegionBasicData>::class.java).toMutableList()){
                val coordinateList = mutableListOf<LatLng>()
                val builder = LatLngBounds.builder()
                for (s in element.Polygon[0].split(" ")){
                    val xyList = s.split(",")
                    val ltlng = LatLng(xyList[0].toDouble(), xyList[1].toDouble())
                    coordinateList.add(ltlng)
                    builder.include(ltlng)
                }
                val polOptions = PolygonOptions()
                val center = builder.build().center
                polOptions.addAll(coordinateList)
                polOptions.strokeWidth(vm.borderThickness)
                vm.myMap.addPolygon(polOptions)
                vm.polygonList[element.Id] = (PolygonData(
                    element.Id,
                    element.Name,
                    coordinateList,
                    "0",
                    null,
                    polOptions,
                    center,
                    null
                ))
            }
        } catch (e: Exception){
            vm.updateFailureOrigin = "Main"
            if (vm.internet.value == true) vm.internet.value = false
        }
    }

    /**
     * Collects measurements for all regions for the given time-interval
     * Parses the info and puts it into the hashtable mainInfoMap with one entry per day
     * @author Permki
     */
    private  fun getData() = MainScope().launch {
        mapProgressbar.show()

        try {
            val response = Fuel.get(getString(R.string.all_regions_details_url).format(getDate(vm.futurePredictionSize - vm.numDays.value!!), getDate(vm.futurePredictionSize - 1))).awaitString()
            vm.internet.value = true
            if (response.isNotEmpty()) {
                for (element in Gson().fromJson(response, Array<AllRegionsDetailsData>::class.java).toMutableList())
                    vm.mainInfoMap[element.Id] = element
                updated_info.text = getString(R.string.updated_time_string).format(GregorianCalendar().createEnhancedFormattedString())
                element_day.text = getDate(vm.currentDaynumber - (vm.numDays.value!! - vm.futurePredictionSize))
            }
            if (vm.polygonList.size == 0) getPolygonData()
            while (vm.polygonList.size == 0) delay(5)
            if (vm.colorMap.value == null) {
                vm.colorMap.value = true
            } else vm.recolorPolygons()
            vm.updateSeekbarSize.value = true
        } catch (e: java.lang.Exception) {
            vm.internet.value = false
        }
        mapProgressbar.hide()
    }

    /**
     * Clears all info drawn on map
     * @author Permki
     */
    override fun onDestroy() {
        super.onDestroy()
        vm.mainInfoMap.clear()
    }


    /**
     * On start, stop all old background activities
     * @author Haakose
     */
    override fun onStart() {
        backgroundWorking(true)
        super.onStart()
    }

    /**
     * on stop send reassurance-notification and start backgroundworking
     */
    override fun onStop() {

        saveUserData() //Saves data fra SharedViewModel (settings tab) in external file
        if (vm.showClosingNotification)
            closingNotification() //Gives the user a notification that the app is running in the background
        if (vm.pushWarningUp || vm.pushWarningDown)
            backgroundWorking() //Sets up a background process, that checks for avalanchedanger
        super.onStop()
    }

    /**
     * When app closes, save key information from sharedviewmodel to external files
     * @author Haakose
     */
    private fun saveUserData(){
        //1. StoredUserData could not save hashlists, so need to transform to mutableList
        val mutL = mutableListOf<Pair<Int, String>>()
        if (!vm.favoritesList.isNullOrEmpty())
            vm.favoritesList.forEach { (key, value) -> mutL.add(Pair(key, value))}
        //2. Background color
        val color = if (vm.colorMap.value != null) vm.colorMap.value!! else true
        //3. Numb of days to show
        val days = vm.numDays.value!!

        //4. Notifikasjoner: Påminnelse at appen kjørere i bakgrunnen       //endre her
        val showClosingNotif = vm.showClosingNotification
        //5. Notifikasjoner: Når faren øker i et av dine favorittområder    //Endres i Backgroundworker.kt
        val showIncrease = vm.pushWarningUp
        //6. Notifikasjoner: Når faren minker i et av dine favorittområder  //Endres i backgroundworker.kt
        val showDecrease = vm.pushWarningDown
        //7. Hvor ofte skal appen sjekke etter endringer fra API
        val checkAPI = vm.checkApiIntervalminutes
        //8. Første gang de åpner appen, ha en egen side med velkomst
        val showWelcome = vm.showWelcomePage
        //9. Karttype: Hvile type kart som skal vises
        val mapType = if (vm.mapType.value != null) vm.mapType.value!! else GoogleMap.MAP_TYPE_TERRAIN


        // Put all the data into a class, and put that class in a list
        val theList = listOf(StoredUserData(days, color, mutL, showIncrease, showDecrease, checkAPI, showClosingNotif, showWelcome, mapType))

        val handler = ExternalFileHandler()
        if(handler.isExternalStorageWritable()){
            val filen = handler.fetchExternalStorageData("UserData", applicationContext)
            if(!handler.writeUserDataFile(filen, theList)) {
                Log.w(tag, "Could not write user data to file" + filen.name)
            }
        }
    }

    /**
     * When app opens, fetch stored information from external files, and edit sharedviewmodel
     * @author Haakose
     */
    private fun fetchUserData(){
        val handler = ExternalFileHandler()
        if(handler.isExternalStorageReadable()){
            val theFile = handler.fetchExternalStorageData("UserData", applicationContext)
            val type = object: TypeToken<List<StoredUserData>>() {}.type
            val theList = handler.readJsonUserDataFile<StoredUserData>(theFile, type)

            if(theList.isEmpty()){
                Log.w(tag, "Opened for first time, if not something is wrong with fetching user data from file: " + theFile.name)
            }
            else{
                //change values in SharedViewModel
                val user = theList[0]

                vm.showWelcomePage = user.showWelcomePage
                vm.mapType.value = user.mapType
                vm.colorMap.value = user.colorMap
                vm.numDays.value = user.daysToShow
                vm.pushWarningUp = user.showAvalancheIncrease
                vm.pushWarningDown = user.showAvalancheDecrease

                vm.checkApiIntervalminutes = user.check_API_IntervalMinutes
                vm.showClosingNotification = user.showClosingNotification
                if (!user.favoritesList.isNullOrEmpty())
                    user.favoritesList.forEach { (v, k) -> vm.favoritesList[v] = k }
            }
        }
    }

    /**
     * When app closes, create a notification that reassures the user the application is running in the background
     * @author Haakose
     */
    private fun closingNotification(){
        if (vm.closingNotificationIsShowedDuringThisRun)
            return

        NotificationHelper.createNotificationChannel(this,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            getString(R.string.app_name), "MainActivity closing notification channel.")

        NotificationHelper.createDataNotification(this,
            getString(R.string.background_running),
            getString(R.string.youll_be_notified),
            getString(R.string.youll_be_notified),
            true,
            R.drawable.ic_close_application_notification,
            MainActivity::class.java)

        vm.closingNotificationIsShowedDuringThisRun = true
    }

    /**
     * When app closes, run a background process that checks if the avalanche risk have changed
     * @author Haakose
     * @param stop  signals that all backgroundprocesses should stop if set to true, used in onStart()
     */
    private fun backgroundWorking(stop: Boolean = false){
        //setup
        val mWorkManager = WorkManager.getInstance(this)

        if (stop){
            mWorkManager.cancelAllWork()
        } else {
            //User wants to know if the avalanche increase or decrease
            val mRequest = PeriodicWorkRequestBuilder<BackgroundWorker>(vm.checkApiIntervalminutes, TimeUnit.MINUTES)
                .setInputData(createInputDataForWorker())
                .build()
            //Setter det igang
            mWorkManager.enqueue(mRequest)
        }
    }

    /**
     * Makes a data class, that functions as input for background process
     * @author Haakose
     * @usage used in backgroundWorking()'s periodicWorkRequestBuilder
     */
    private fun createInputDataForWorker(): Data {
        //Create a Data.Builder to send
        val builder = Data.Builder()

        //Transform hashmap to one string array, and one IntArray
        val hashList = vm.favoritesList
        val listStrID = ArrayList(hashList.values)
        val listIntLvl = ArrayList(hashList.keys)

        val arrStrID = arrayOfNulls<String>(listStrID.size)
        val arrIntLvl = arrayOfNulls<Int>(listIntLvl.size)

        listStrID.toArray(arrStrID)
        listIntLvl.toArray(arrIntLvl)

        val arrInt = IntArray(listIntLvl.size)

        var counter = 0
        for(e in arrIntLvl) {
            arrInt[counter] = e!!
            counter += 1
        }

        builder.putIntArray("1", arrInt)
        builder.putStringArray("2", arrStrID)

        //Send information of which notification to show (increase/decrease)
        var increase = 1
        var decrease = 1
        if (!vm.pushWarningUp) increase = 0
        if (!vm.pushWarningDown) decrease = 0
        val avalInt : IntArray = intArrayOf(increase , decrease)

        builder.putIntArray("3", avalInt)

        /*
        TESTER at increase notifikasjonene funker, sjekk hva Jotunheimen har idag (1)
        Indre sogn decrease fra (4) til (2)

        val arrIntTest: IntArray = intArrayOf(3028, 3029)
        val arrStrIDTest = arrayOf("1", "4")

        builder.putIntArray("1", arrIntTest)
        builder.putStringArray("2", arrStrIDTest)
        builder.putIntArray("3", avalInt)
         */


        return builder.build()
    }

    private fun deleteUserData(){

        vm.deleteUserData.observe(this, Observer{
            if(vm.deleteUserData.value!!){ //Initialised to "false" by default
                val handler = ExternalFileHandler()
                if(handler.deleteUserDataFile("UserData", applicationContext)){
                    getString(R.string.u_data_deleted) .toast(this)
                    //Reset settings in viewModel
                    vm.favoritesList.clear()
                    vm.colorMap.value = true
                    vm.numDays.value = 7
                    vm.showClosingNotification = true
                    vm.pushWarningDown = true
                    vm.pushWarningUp = true
                    vm.checkApiIntervalminutes = 15
                    vm.showWelcomePage = true
                    vm.mapType.value = 0

                }
                else {
                    "Det skjedde en feil under sletting av dine brukerdata".toast(this)
                    Log.w(tag, "Could not delete file UserData after pushing button 'slett mine lagrede brukerdata' from settings")
                }
            }
        })
    }


}
