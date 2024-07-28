package com.iyam.catatanku.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.iyam.catatanku.R
import com.iyam.catatanku.ads.AdsManagerImpl
import com.iyam.catatanku.data.FileCallback
import com.iyam.catatanku.data.NoteRepositoryImpl
import com.iyam.catatanku.databinding.ActivityNoteDetailBinding

class NoteDetailActivity : AppCompatActivity() {

    private val binding: ActivityNoteDetailBinding by lazy {
        ActivityNoteDetailBinding.inflate(
            layoutInflater,
            window.decorView.findViewById(android.R.id.content),
            false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnSave.isVisible = true
        setOnClickListener()
        setContentForUpdate()
        setAdManager()
    }

    private fun setAdManager() {
        AdsManagerImpl(this).initAd()
        AdsManagerImpl(this).showBannerAd(binding.llAdsContainer)
    }

    private fun setContentForUpdate() {
        val extras = intent.extras
        if (extras != null) {
            supportActionBar?.setTitle(R.string.ubah_catatan_text)
            binding.etFileName.setText(extras.getString(FILENAME))
            binding.etContent.setText(extras.getString(FILE_CONTENT))
            binding.btnSave.isVisible = false
            binding.btnUpdate.isVisible = true
            binding.etFileName.isEnabled = false
        }
    }

    private fun setOnClickListener() {
        binding.btnSave.setOnClickListener{
            saveFile()
        }
        binding.btnUpdate.setOnClickListener{
            updateFile()
        }
    }

    private fun updateFile(){
        val fileName = binding.etFileName.text.toString().trim()
        val fileContent = binding.etContent.text.toString()
        val repo = NoteRepositoryImpl(this)
        repo.updateFile(fileName, fileContent, object: FileCallback{
            override fun onSuccess() {
                Toast.makeText(this@NoteDetailActivity, R.string.note_saved_text, Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@NoteDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveFile() {
        val fileName = binding.etFileName.text.toString().trim()
        val fileContent = binding.etContent.text.toString()
        val repo = NoteRepositoryImpl(this)
        repo.createFile(fileName, fileContent, object: FileCallback{
            override fun onSuccess() {
                Toast.makeText(this@NoteDetailActivity, R.string.note_saved_text, Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@NoteDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object{
        const val FILENAME = "filename"
        private const val FILE_CONTENT = "fileContent"

        fun startActivity(context: Context){
            val intent = Intent(context, NoteDetailActivity::class.java)
            context.startActivity(intent)
        }
        fun startActivityUpdate(context: Context, fileName: String, fileContent: String){
            val intent = Intent(context, NoteDetailActivity::class.java)
            intent.putExtra(FILENAME, fileName)
            intent.putExtra(FILE_CONTENT, fileContent)
            context.startActivity(intent)
        }
    }
}