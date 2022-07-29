package com.sample.sms

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.sms.ui.theme.SampleSmsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. When input field 1 then auto move to field 2
// 2. use viewMode trigger to update all field
// 3.
class MainActivity : ComponentActivity(), HomeEvent {

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleSmsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    HomeScreen(viewModel)
                }
            }
        }
    }

    override fun toast(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}

interface HomeEvent{
    fun toast(message: String)
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(viewModel: HomeViewModel? = null,event: HomeEvent? = null) {
    var sms by mutableStateOf(viewModel?.sms ?: List(6) {
            "1"
        })

    Column(modifier = Modifier.fillMaxWidth()) {
        SmsTextFieldBar(sms) {
            sms = it
            event?.toast("SMS:${it}")
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = {
                viewModel?.trigger()
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFd22630),
                contentColor = Color.White
            ), modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Trigger counter")
        }
    }
}

class HomeViewModel : ViewModel() {

    private var count = 0

    var sms by mutableStateOf(List(6) {
        ""
    })
        private set

    init {
        trigger()
    }

    // trigger to update ui
    fun trigger() {
        count++
        viewModelScope.launch {
            delay(500L)
            sms = List(6) {
                count.toString()
            }
        }
    }
}

@Composable
fun CommonOtpTextField(
    otp: String,
    focusRequester: FocusRequester,
    onChange: (String) -> Unit
) {
    var otpValue by remember {
        mutableStateOf(otp)
    }

    OutlinedTextField(
        value = otpValue,
        singleLine = true,
        onValueChange = {
            if (it.length <= 1) {
                otpValue = it
                onChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(50.dp)
            .focusRequester(focusRequester),
        maxLines = 1,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        )
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun SmsTextFieldBar(
    otp: List<String>,
    onDataChange: OnSmsTextFieldChange? = null
) {
    val otp2 by mutableStateOf(otp)
    val size = otp2.size
    val focusRequesters = remember {
        MutableList(size) { FocusRequester() }
    }
    val lastFocusIndex = size - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, start = 15.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LazyRow {
            itemsIndexed(otp2) { index, item ->
                CommonOtpTextField(otp = item, focusRequesters[index]) { fieldValue ->
                    val focusToNext = index != lastFocusIndex && fieldValue.isNotEmpty()
                    if (focusToNext) {
                        focusRequesters[index + 1].requestFocus()
                    }
                    onDataChange?.invoke(otp2.map {
                        it
                    })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SampleSmsTheme {
        HomeScreen()
    }
}

typealias OnSmsTextFieldChange = (List<String>) -> Unit