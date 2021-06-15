package com.goel.peerlocator.activities

  import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
  import android.view.WindowManager
  import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.LocationMembersAdapter
import com.goel.peerlocator.databinding.ActivityCircleBinding
import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.LocationMemberModel
import com.goel.peerlocator.models.MemberModel
import com.goel.peerlocator.repositories.CirclesRepository
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
  import com.google.android.gms.maps.SupportMapFragment
  import com.google.android.gms.maps.model.LatLng

class CircleActivity : AppCompatActivity(), CircleDataListener,
    LocationMembersAdapter.ClickListener, OnMapReadyCallback {

    companion object {
        lateinit var model: CircleModel
    }

    private lateinit var binding: ActivityCircleBinding
    private lateinit var mMap: GoogleMap
    private lateinit var membersList: ArrayList<LocationMemberModel>
    private lateinit var adapter: LocationMembersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toggleMembers()
        createRecyclerView()
        getAllMembers()
    }

    override fun onResume() {
        super.onResume()
        startMyLocation()
    }

    private fun startMyLocation () {
        try {
            ServicesHandler.stopBackgroundLocation(this)
            if (ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                ServicesHandler.startBackgroundLocation(this)
            }
            else {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
            finish()
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        findMe()
        binding.findMe.setOnClickListener {
            findMe()
        }
    }

    private fun findMe () {
        val myLocation = mMap.myLocation
        if (myLocation == null)
            Toast.makeText(this, "Getting your Location", Toast.LENGTH_SHORT).show()
        myLocation?.let {
            if (checkGPS()) {
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM))
            }
            else
                alertForGPS()
        }
    }

    private fun checkGPS(): Boolean {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun alertForGPS() {
        AlertDialog.Builder(this)
            .setTitle("Turn On GPS")
            .setMessage("We recommend you to turn on GPS for better accuracy")
            .setPositiveButton("Settings") {dialog, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }.show()
    }

    private fun createRecyclerView () {
        membersList = ArrayList()
        adapter = LocationMembersAdapter(membersList, this, this)
        binding.membersRecyclerView.adapter = adapter
        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.membersRecyclerView.layoutManager = lm
        binding.findFriend.setOnClickListener {
            toggleMembers()
        }
    }

    private fun toggleMembers () {
        val visibility = binding.membersRecyclerView.visibility
        if (visibility == View.VISIBLE) {
            binding.membersRecyclerView.translationX = -200f
            binding.membersRecyclerView.visibility = View.GONE
        }
        else {
            binding.membersRecyclerView.visibility = View.VISIBLE
            binding.membersRecyclerView.animate().translationXBy(200f).duration = 200
        }
    }

    private fun getAllMembers () {
        CirclesRepository.instance.getAllMembers(model.documentReference, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        val preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        val per = preferences.getBoolean(Constants.BACK_LOC, true)
        if (!per) {
            ServicesHandler.stopBackgroundLocation(this)
        }
    }

    override fun onMemberCountComplete(members: Long) {}

    override fun onMemberRetrieved(member: MemberModel) {
        val memberModel = LocationMemberModel(documentReference = member.documentReference,
            uid = member.uid, name = member.name, imageUrl = member.imageUrl)
        membersList.add(memberModel)
        adapter.notifyDataSetChanged()
    }

    override fun onError() {
        Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
    }

    override fun onMemberClicked(position: Int) {
        TODO("Not yet implemented")
    }
}