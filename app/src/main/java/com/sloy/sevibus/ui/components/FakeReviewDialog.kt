package com.sloy.sevibus.ui.components

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.DialogFragment
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun FakeReviewDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FakeReviewDialogContent(onDismiss = onDismiss)
        }
    }
}

@Composable
private fun FakeReviewDialogContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column {
            Text(
                "Not Google Play",
                color = SevTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 16.dp)
            )
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())

            ) {
                Row {
                    AppIcon()
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(text = "SeviBus")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Las reseñas incluyen la información de tu cuenta y de tu dispositivo, y solo los desarrolladores pueden verlas. Más información",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )

                        Spacer(modifier = Modifier.height(28.dp))
                        StarsRating()
                        Spacer(modifier = Modifier.height(36.dp))

                    }
                }
                Buttons(onDismiss)
            }
        }
    }
}

@Composable
private fun AppIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.error,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "S",
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Buttons(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(end = 12.dp)
                .fillMaxWidth()
                .weight(0.5f)
        ) {
            Text(
                text = "Ahora no",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Button(
            onClick = { /* Non-interactive for Enviar */ },
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
        ) {
            Text(
                text = "Enviar",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun StarsRating() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(5) {
            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = "Star rating ${it + 1}",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview
@Composable
private fun FakeReviewDialogContentPreview() {
    SevTheme {
        // Now previewing the content directly
        FakeReviewDialogContent(onDismiss = {})
    }
}

class FakeReviewDialogFragment : DialogFragment() {

    private var dismissalCallback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar).apply {
            setCanceledOnTouchOutside(true)
            window?.apply {
                setFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                )
                setDimAmount(0.5f)
                setGravity(Gravity.BOTTOM)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SevTheme {
                    FakeReviewDialog(
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        dismissalCallback?.invoke()
    }

    /**
     * Suspends until the dialog is dismissed by the user.
     */
    suspend fun awaitDismissal() = suspendCancellableCoroutine<Unit> { continuation ->
        dismissalCallback = {
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }

        continuation.invokeOnCancellation {
            dismissalCallback = null
        }
    }
}
