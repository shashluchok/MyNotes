package com.shashluchok.medianotes.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.components.LocalNavAnimatedVisibilityScope
import com.shashluchok.medianotes.presentation.components.LocalSharedTransitionScope
import com.shashluchok.medianotes.presentation.screen.cameracapture.CameraCaptureScreen
import com.shashluchok.medianotes.presentation.screen.imageeditor.ImageEditorScreen
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNotesScreen
import com.shashluchok.medianotes.presentation.screen.sketch.SketchScreen

internal enum class MediaNoteRoute {
    MediaNotesList, Sketch, Camera,
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MediaNotesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    SharedTransitionLayout(
        modifier = modifier
    ) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            NavHost(
                navController = navController,
                startDestination = MediaNoteRoute.MediaNotesList.name,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    route = MediaNoteRoute.MediaNotesList.name
                ) {
                    CompositionLocalProvider(
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        MediaNotesScreen(
                            onOpenCamera = {
                                navController.navigate(MediaNoteRoute.Camera.name)
                            },
                            onOpenImage = { item ->
                                navController.navigate(item)
                            },
                            onSketchClick = { navController.navigate(MediaNoteRoute.Sketch.name) }
                        )
                    }
                }
                composable(
                    route = MediaNoteRoute.Sketch.name,
                    enterTransition = {
                        slideInVertically { it }
                    },
                    exitTransition = {
                        slideOutVertically { it }
                    }
                ) {
                    SketchScreen(
                        onDismissRequest = navController::popBackStack
                    )
                }

                composable(
                    route = MediaNoteRoute.Camera.name
                ) {
                    CompositionLocalProvider(
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        CameraCaptureScreen(
                            onPhotoSaved = {
                                navController.navigate(it)
                            }
                        )
                    }
                }

                composable<MediaImage> { backStackEntry ->
                    val image: MediaImage = backStackEntry.toRoute()
                    CompositionLocalProvider(
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        ImageEditorScreen(
                            image = image,
                            modifier = Modifier,
                            onDismiss = navController::popBackStack
                        )
                    }
                }
            }
        }
    }
}
