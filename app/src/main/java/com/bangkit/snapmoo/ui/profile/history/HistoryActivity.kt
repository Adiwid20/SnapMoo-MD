package com.bangkit.snapmoo.ui.profile.history

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.snapmoo.R
import com.bangkit.snapmoo.data.Result
import com.bangkit.snapmoo.databinding.ActivityHistoryBinding
import com.bangkit.snapmoo.ui.adapter.ListHistoryAdapter
import com.bangkit.snapmoo.ui.MainViewModelFactory

class HistoryActivity : AppCompatActivity(),View.OnClickListener {
    private lateinit var binding: ActivityHistoryBinding
    private val historyViewModel by viewModels<HistoryViewModel> {
        MainViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()
        setupAction()
    }

    private fun setupAction() {
        historyViewModel.getSession().observe(this) { user ->
            val token = user.token
            if (token.isNotEmpty()) {
                historyViewModel.getAllHistory(token).observe(this) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }

                        is Result.Success -> {
                            showLoading(false)
                            val data = result.data.data
                            val layoutManager = LinearLayoutManager(this)
                            binding.rvListHistory.layoutManager = layoutManager
                            val adapter = ListHistoryAdapter()
                            adapter.submitList(data)
                            binding.rvListHistory.adapter = adapter

                            adapter.onClick = {
                                val checkIsSaved = !(it.isSaved)
                                historyViewModel.addBookmark(token, it.historyId, checkIsSaved)
                                    .observe(this) { result ->
                                        when (result) {
                                            is Result.Loading -> {
                                            }

                                            is Result.Success -> {
                                                showToast("Data berhasil ditambahkan")
                                                setupAction()
                                            }

                                            is Result.Error -> {
                                            }

                                        }
                                    }
                            }
                        }

                        is Result.Error -> {
                            showLoading(false)
                            showToast(result.error)
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvListHistory.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
    private fun setToolbar() {
        binding.toolbar.btnBackToolbar.setOnClickListener {
            onBackPressed()
        }
        val label = getActivityLabel()
        binding.toolbar.tvToolbarTitle.text = label
    }

    private fun getActivityLabel(): String {
        return try {
            val activityInfo = packageManager.getActivityInfo(componentName, 0)
            activityInfo.loadLabel(packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            title.toString()  // default title
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_back_toolbar -> {
                finish()
            }
        }
    }
}