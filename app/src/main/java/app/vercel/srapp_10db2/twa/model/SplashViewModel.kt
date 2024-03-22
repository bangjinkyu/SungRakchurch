package app.vercel.srapp_10db2.twa.model

import androidx.compose.runtime.collection.mutableVectorOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebViewNavigator
import com.google.accompanist.web.WebViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SplashViewModel: ViewModel() {

    private val mutableStateFlow = MutableStateFlow(true)
    val isLoading = mutableStateFlow.asStateFlow()

    var weblink = "https://srapp.vercel.app/"

    val webViewState = WebViewState(
        WebContent.Url(
            url = weblink,
            additionalHttpHeaders = emptyMap()
        )
    )
    val webViewNavigator = WebViewNavigator(viewModelScope)

    init {
        viewModelScope.launch {
            delay(2000)
            mutableStateFlow.value = false
        }
    }
}