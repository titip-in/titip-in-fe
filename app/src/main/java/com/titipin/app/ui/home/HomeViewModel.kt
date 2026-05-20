package com.titipin.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.model.UserTier
import com.titipin.app.data.model.normalizedTier
import com.titipin.app.data.model.tierActiveLimit
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.PrelovedRequestRepository
import com.titipin.app.data.repository.RequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val userId: String? = null,
    val userInitials: String = "",
    val userAvatarUrl: String? = null,
    val userTier: String = UserTier.BASIC,
    val userBoostQuota: Int = 0,
    val userTierExpiredAt: String? = null,
    val activeMineCount: Int = 0,
    val activeLimit: Int = 3,
    // ── Raw data ────────────────────────────────────────────────────────────
    val allJastip: List<JastipDto> = emptyList(),
    val allPreloved: List<PrelovedDto> = emptyList(),
    val allJastipRequests: List<RequestDto> = emptyList(),
    val allPrelovedRequests: List<PrelovedRequestDto> = emptyList(),
    // ── Derived: recent activity feed ───────────────────────────────────
    val recentJastip: List<JastipDto> = emptyList(),
    val recentPreloved: List<PrelovedDto> = emptyList(),
    val recentJastipRequests: List<RequestDto> = emptyList(),
    val recentPrelovedRequests: List<PrelovedRequestDto> = emptyList(),
    // ── Counts ────────────────────────────────────────────────────────────
    val jastipCount: Int = 0,
    val prelovedCount: Int = 0,
    val jastipRequestCount: Int = 0,
    val prelovedRequestCount: Int = 0,
    // ── State ────────────────────────────────────────────────────────────
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val showSetupProfile: Boolean = false,   // true jika WA number belum diisi
    // ── Search ────────────────────────────────────────────────────────────
    val searchQuery: String = "",
    val searchJastip: List<JastipDto> = emptyList(),
    val searchPreloved: List<PrelovedDto> = emptyList(),
)

val HomeUiState.isSearchActive get() = searchQuery.isNotBlank()

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val jastipRepository: JastipRepository,
    private val prelovedRepository: PrelovedRepository,
    private val requestRepository: RequestRepository,
    private val prelovedRequestRepository: PrelovedRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            val name = dataStore.userName.first() ?: ""
            val userId = dataStore.userId.first()
            val wa   = dataStore.userWaNumber.first() ?: ""
            val avatarUrl = dataStore.userAvatarUrl.first()
            val tier = dataStore.userTier.first().normalizedTier()
            val boostQuota = dataStore.userBoostQuota.first()
            val tierExpiredAt = dataStore.userTierExpiredAt.first()
            val initials = name.trim()
                .split(" ").filter { it.isNotBlank() }
                .take(2).joinToString("") { it.first().uppercase() }

            _uiState.value = _uiState.value.copy(
                userName         = name,
                userId           = userId,
                userInitials     = initials,
                userAvatarUrl    = avatarUrl,
                userTier         = tier,
                userBoostQuota   = boostQuota,
                userTierExpiredAt = tierExpiredAt,
                activeLimit      = tierActiveLimit(tier),
                showSetupProfile = wa.isBlank() && name.isNotBlank()
            )

            // Paralel fetch 4 endpoint publik
            val jastipDeferred          = async { jastipRepository.getJastipList() }
            val prelovedDeferred         = async { prelovedRepository.getPrelovedList() }
            val jastipRequestDeferred    = async { requestRepository.getRequestList() }
            val prelovedRequestDeferred  = async { prelovedRequestRepository.getPrelovedRequestList() }

            val allJastip           = (jastipDeferred.await()         as? Result.Success)?.data ?: emptyList()
            val allPreloved         = (prelovedDeferred.await()        as? Result.Success)?.data ?: emptyList()
            val allJastipRequests   = (jastipRequestDeferred.await()   as? Result.Success)?.data ?: emptyList()
            val allPrelovedRequests = (prelovedRequestDeferred.await() as? Result.Success)?.data ?: emptyList()

            _uiState.value = _uiState.value.copy(
                allJastip           = allJastip,
                allPreloved         = allPreloved,
                allJastipRequests   = allJastipRequests,
                allPrelovedRequests = allPrelovedRequests,
                // Recent: 2 item tiap kategori untuk activity feed
                recentJastip           = allJastip.take(2),
                recentPreloved         = allPreloved.take(2),
                recentJastipRequests   = allJastipRequests.take(2),
                recentPrelovedRequests = allPrelovedRequests.take(2),
                // Stats
                jastipCount          = allJastip.count { it.status == "ACTIVE" },
                prelovedCount        = allPreloved.count { it.status == "AVAILABLE" },
                jastipRequestCount   = allJastipRequests.count { it.status == "OPEN" },
                prelovedRequestCount = allPrelovedRequests.count { it.status == "OPEN" },
                activeMineCount = listOf(
                    allJastip.count { it.userId == userId && it.status == "ACTIVE" },
                    allPreloved.count { it.userId?.toString() == userId && it.status == "AVAILABLE" },
                    allJastipRequests.count { it.userId?.toString() == userId && it.status == "OPEN" },
                    allPrelovedRequests.count { it.userId?.toString() == userId && it.status == "OPEN" }
                ).maxOrNull() ?: 0,
                isLoading = false
            )
        }
    }

    fun dismissSetupProfile() {
        _uiState.value = _uiState.value.copy(showSetupProfile = false)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank() || query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(
                searchJastip = emptyList(), searchPreloved = emptyList()
            )
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            val q = query.trim()
            val jastipDeferred = async { jastipRepository.searchJastip(q) }
            val prelovedDeferred = async { prelovedRepository.searchPreloved(q) }
            _uiState.value = _uiState.value.copy(
                searchJastip = (jastipDeferred.await() as? Result.Success)?.data ?: emptyList(),
                searchPreloved = (prelovedDeferred.await() as? Result.Success)?.data ?: emptyList()
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "", searchJastip = emptyList(), searchPreloved = emptyList()
        )
    }
}
