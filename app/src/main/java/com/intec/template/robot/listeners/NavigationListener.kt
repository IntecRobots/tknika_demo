package com.intec.template.robot.listeners

interface NavigationListener {
    fun onRouteBlocked()
    fun onObstacleDisappeared()
    fun onNavigationStarted()
}