package com.titipin.app.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ApiModelMappingTest {

    private val gson = Gson()

    @Test
    fun authResponse_mapsSnakeCaseFields() {
        val type = object : TypeToken<ApiResponse<AuthResponse>>() {}.type
        val json = """
            {
              "success": true,
              "message": "OK",
              "data": {
                "access_token": "token-123",
                "token_type": "Bearer",
                "user": {
                  "id": 7,
                  "name": "Okta",
                  "email": "okta@example.com",
                  "wa_number": "628123456789",
                  "avatar_url": "https://cdn.titipin.me/avatar.jpg",
                  "status": "Mahasiswa Filkom"
                }
              }
            }
        """.trimIndent()

        val response = gson.fromJson<ApiResponse<AuthResponse>>(json, type)

        assertEquals(true, response.success)
        assertEquals("token-123", response.data?.accessToken)
        assertEquals("Bearer", response.data?.tokenType)
        assertEquals(7, response.data?.user?.id)
        assertEquals("628123456789", response.data?.user?.waNumber)
        assertEquals("https://cdn.titipin.me/avatar.jpg", response.data?.user?.avatarUrl)
    }

    @Test
    fun jastipListing_mapsWebApiFieldsAndPrimaryImage() {
        val json = """
            {
              "id": "12",
              "user_id": "7",
              "user": {
                "id": 7,
                "name": "Okta",
                "wa_number": "628123456789",
                "avatar_url": "https://cdn.titipin.me/avatar.jpg",
                "status": "Mahasiswa Filkom"
              },
              "category_id": 3,
              "category": {
                "id": 3,
                "name": "Minuman Viral & Dessert",
                "icon": "🥤",
                "type": "jastip"
              },
              "title": "ada yang mau nitip calf?",
              "description": "Ada catatan",
              "from_loc": "Calf Coffee",
              "to_loc": "Sumbersari",
              "deadline": "2026-05-17T18:52:00",
              "images": [
                { "id": 1, "image_url": "https://cdn.titipin.me/secondary.jpg", "is_primary": false },
                { "id": 2, "image_url": "https://cdn.titipin.me/primary.jpg", "is_primary": true }
              ],
              "primary_image_url": "https://cdn.titipin.me/fallback.jpg",
              "status": "ACTIVE",
              "created_at": "2026-05-17T10:00:00"
            }
        """.trimIndent()

        val item = gson.fromJson(json, JastipDto::class.java)

        assertEquals("7", item.userId)
        assertEquals(3, item.categoryId)
        assertEquals("Ada catatan", item.notes)
        assertEquals("Calf Coffee", item.fromLocation)
        assertEquals("Sumbersari", item.toLocation)
        assertEquals("https://cdn.titipin.me/primary.jpg", item.primaryImageUrl())
    }

    @Test
    fun prelovedListing_formatsPriceConditionAndFallbackImage() {
        val json = """
            {
              "id": "22",
              "user_id": 7,
              "title": "Sepatu",
              "description": "Masih bagus",
              "price": 125000,
              "condition": "LIKE_NEW",
              "images": [],
              "primary_image_url": "https://cdn.titipin.me/shoes.jpg",
              "status": "AVAILABLE"
            }
        """.trimIndent()

        val item = gson.fromJson(json, PrelovedDto::class.java)

        assertEquals("Rp 125.000", item.formattedPrice())
        assertEquals("Seperti Baru", item.conditionLabel())
        assertEquals("https://cdn.titipin.me/shoes.jpg", item.primaryImageUrl())
    }

    @Test
    fun prelovedRequest_mapsMaxPriceAndFormatsIt() {
        val json = """
            {
              "id": "32",
              "user_id": 7,
              "title": "Kalkulator",
              "description": "Butuh scientific calculator",
              "max_price": 250000,
              "status": "OPEN"
            }
        """.trimIndent()

        val item = gson.fromJson(json, PrelovedRequestDto::class.java)

        assertEquals(250000, item.maxPrice)
        assertEquals("~Rp 250.000", item.formattedMaxPrice())
    }

    @Test
    fun prelovedRequest_withoutMaxPriceReturnsNullFormattedPrice() {
        val item = PrelovedRequestDto(
            id = "33",
            title = "Kalkulator",
            description = null,
            maxPrice = null,
            status = "OPEN"
        )

        assertNull(item.formattedMaxPrice())
    }

    @Test
    fun paginatedResponse_mapsPaginationFields() {
        val type = object : TypeToken<ApiResponse<PaginatedResponse<PrelovedRequestDto>>>() {}.type
        val json = """
            {
              "success": true,
              "data": {
                "current_page": 1,
                "data": [
                  {
                    "id": "1",
                    "title": "Kalkulator",
                    "description": null,
                    "max_price": null,
                    "status": "OPEN"
                  }
                ],
                "per_page": 15,
                "total": 1,
                "next_page_url": null,
                "prev_page_url": null
              }
            }
        """.trimIndent()

        val response = gson.fromJson<ApiResponse<PaginatedResponse<PrelovedRequestDto>>>(json, type)

        assertEquals(1, response.data?.currentPage)
        assertEquals(15, response.data?.perPage)
        assertEquals(1, response.data?.total)
        assertFalse(response.data?.data.orEmpty().isEmpty())
        assertEquals("Kalkulator", response.data?.data?.first()?.title)
    }
}
