package com.aitranslator.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun `bottom navigation exposes exactly six items`() {
        assertEquals(6, Screen.bottomNavItems.size)
    }

    @Test
    fun `bottom navigation routes are unique`() {
        val routes = Screen.bottomNavItems.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun `bottom navigation items follow the expected order`() {
        val expectedRoutes = listOf(
            "translate", "conversation", "camera", "dictionary", "phrases", "more"
        )
        assertEquals(expectedRoutes, Screen.bottomNavItems.map { it.route })
    }

    @Test
    fun `translate is the start screen of the bottom navigation`() {
        assertEquals(Screen.Translate, Screen.bottomNavItems.first())
    }

    @Test
    fun `each bottom navigation item has a distinct label resource`() {
        val labelResIds = Screen.bottomNavItems.map { it.labelRes }
        assertEquals(labelResIds.size, labelResIds.toSet().size)
    }
}