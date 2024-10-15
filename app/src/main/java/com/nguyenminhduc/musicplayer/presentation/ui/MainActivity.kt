package com.nguyenminhduc.musicplayer.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nguyenminhduc.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewPager get() = binding.viewPager
    private val tabLayout get() = binding.tabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main as View) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupUI()
    }

    private fun setupUI() {
        setupViewPager()
    }

    private fun setupViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(fragmentManager = supportFragmentManager, lifecycle = lifecycle)
        viewPagerAdapter.addFragment(SongListFragment(), "Songs")
        viewPagerAdapter.addFragment(AlbumFragment(), "Album")
        viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = viewPagerAdapter.titles[position]
        }.attach()
    }
}