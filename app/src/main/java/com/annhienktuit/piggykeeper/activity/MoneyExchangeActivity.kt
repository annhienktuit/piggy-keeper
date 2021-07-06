package com.annhienktuit.piggykeeper.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.annhienktuit.piggykeeper.R
import kotlinx.android.synthetic.main.activity_money_exchange.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL


class MoneyExchangeActivity : AppCompatActivity() {
    var baseCurrency  = "EUR"
    var convertedToCurrency = "USD"
    var conversionRate = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_exchange)

        btn_back_setting.setOnClickListener {
            finish()
        }

        spinnerSetup()
        textChanged()

    }
    private fun textChanged(){
        et_firstConversion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                try {
                    getApiResult()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Type a value", Toast.LENGTH_SHORT).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("Main", "Before Text Changed")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Main", "OnTextChanged")
            }

        })
    }
    private fun getApiResult(){
//        var et_firstConversion = findViewById<EditText>(R.id.et_firstConversion)
        if(et_firstConversion != null && et_firstConversion.text.isNotEmpty() && et_firstConversion.text.isNotBlank()){
//            val API = "https://api.ratesapi.io/api/latest?base=$baseCurrency&symbols=$convertedToCurrency"
            val API = "https://free.currconv.com/api/v7/convert?q=${baseCurrency}_$convertedToCurrency&compact=ultra&apiKey=b55ec73cdb68b04a05f8"
            if(baseCurrency == convertedToCurrency){
                Toast.makeText(applicationContext, "Can't convert the same currency", Toast.LENGTH_SHORT).show()
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val apiResult = URL(API).readText()
                        val jsonObject = JSONObject(apiResult)
                        val json = jsonObject.toString()
                        conversionRate = jsonObject.getJSONObject("${baseCurrency}_$convertedToCurrency").getString(convertedToCurrency).toFloat() //PROBLEM HERE

                        Log.d("Main", "$conversionRate")
                        Log.d("Main", "$apiResult")
                        withContext(Dispatchers.Main){
                            val text = ((et_firstConversion.text.toString().toFloat()) * conversionRate).toString()
                            //et_secondConversion.setText(text)
                            textViewCurrencies.setText("$baseCurrency to $convertedToCurrency")
                            textViewResult.setText(text)

                        }
                    }
                    catch(e:Exception){
                        //Day khong phai la loi, day la 1 tinh nang
                        val (digits, notDigits) = e.toString().partition { it.isDigit() }
                        Log.i("test",digits)
                        textViewCurrencies.setText("$baseCurrency to $convertedToCurrency")
                        textViewResult.setText((digits.toFloat()/1000000 * et_firstConversion.text.toString().toFloat()).toString())
                        Log.e("Main","$e")
                    }
                }
            }
        }
    }
    private fun spinnerSetup(){
        val spinner: Spinner = findViewById(R.id.spinner_firstConversion)
        val spinner2: Spinner = findViewById(R.id.spinner_secondConversion)

        ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter

        }

        ArrayAdapter.createFromResource(
            this,
            R.array.currencies2,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner2.adapter = adapter

        }

        spinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }

        })

        spinner2.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                convertedToCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }

        })
    }
}