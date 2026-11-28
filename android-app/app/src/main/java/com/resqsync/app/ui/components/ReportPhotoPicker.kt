package com.resqsync.app.ui.components

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

private const val MAX_PHOTOS = 3
private const val MAX_SOURCE_BYTES = 20L * 1024 * 1024
private const val MAX_COMPRESSED_BYTES = 8 * 1024 * 1024

private data class SelectedPhoto(
    val source: String,
    val path: String,
    val checksum: String
)

@Composable
fun ReportPhotoPicker(
    onPhotoPathsChanged: (List<String>) -> Unit,
    onImportingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photos by remember { mutableStateOf(emptyList<SelectedPhoto>()) }
    var isImporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewPath by remember { mutableStateOf<String?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    val storageDirectory = remember(context) {
        File(context.filesDir, "photo_evidence/pending").apply { mkdirs() }
    }

    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val availableSlots = MAX_PHOTOS - photos.size
        if (availableSlots <= 0) {
            errorMessage = "A report can include up to 3 photos."
            return
        }

        val uniqueUris = uris
            .distinctBy(Uri::toString)
            .filterNot { uri -> photos.any { it.source == uri.toString() } }
            .take(availableSlots)
        if (uniqueUris.isEmpty()) {
            errorMessage = "Those photos are already attached."
            return
        }

        isImporting = true
        onImportingChanged(true)
        errorMessage = null
        scope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    uniqueUris.map { uri ->
                        runCatching { importPhotoToPrivateStorage(context, uri, storageDirectory) }
                    }
                }
                val imported = results.mapNotNull { result -> result.getOrNull() }
                val checksums = photos.mapTo(mutableSetOf()) { it.checksum }
                val uniquePhotos = imported.filter { photo ->
                    if (checksums.add(photo.checksum)) {
                        true
                    } else {
                        File(photo.path).delete()
                        false
                    }
                }
                photos = photos + uniquePhotos
                onPhotoPathsChanged(photos.map { it.path })
                val failure = results.firstOrNull { it.isFailure }?.exceptionOrNull()
                errorMessage = failure?.message
                    ?: if (uniquePhotos.size < imported.size) "Duplicate photos were removed." else null
            } finally {
                isImporting = false
                onImportingChanged(false)
            }
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS),
        onResult = ::importUris
    )
    val cameraPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        val cameraFile = pendingCameraFile
        pendingCameraFile = null
        if (saved && cameraFile != null) {
            importUris(listOf(Uri.fromFile(cameraFile)))
        } else if (cameraFile != null) {
            errorMessage = "The camera did not save a photo."
        }
        cameraFile?.delete()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Attach photos (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add up to 3 photos. They are saved privately on this device with the report.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        val file = File(context.cacheDir, "photo_capture_${UUID.randomUUID()}.jpg")
                        file.parentFile?.mkdirs()
                        pendingCameraFile = file
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            cameraPicker.launch(uri)
                        } catch (e: ActivityNotFoundException) {
                            pendingCameraFile = null
                            file.delete()
                            errorMessage = "No camera app is available on this device."
                        }
                    },
                    enabled = photos.size < MAX_PHOTOS && !isImporting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Take photo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        galleryPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = photos.size < MAX_PHOTOS && !isImporting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Choose photos", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            if (isImporting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Preparing photos…", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(photos, key = { it.path }) { photo ->
                        Column(
                            modifier = Modifier.width(104.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box {
                                PhotoThumbnail(
                                    path = photo.path,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clickable { previewPath = photo.path }
                                )
                                TextButton(
                                    onClick = {
                                        File(photo.path).delete()
                                        photos = photos.filterNot { it.path == photo.path }
                                        onPhotoPathsChanged(photos.map { it.path })
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("×", fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Saved on device",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = "${photos.size} of $MAX_PHOTOS photos attached",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    previewPath?.let { path ->
        Dialog(onDismissRequest = { previewPath = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhotoThumbnail(
                        path = path,
                        maxDimension = 1800,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp),
                        contentScale = ContentScale.Fit
                    )
                    TextButton(
                        onClick = { previewPath = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close preview")
                    }
                }
            }
        }
    }
}

@Composable
internal fun PhotoThumbnail(
    path: String,
    modifier: Modifier = Modifier,
    maxDimension: Int = 320,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, path, maxDimension) {
        value = withContext(Dispatchers.IO) {
            decodeSampledBitmap(File(path), maxDimension)
        }
    }

    if (bitmap == null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Preview unavailable", style = MaterialTheme.typography.labelSmall)
            }
        }
    } else {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Attached emergency photo",
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

private fun importPhotoToPrivateStorage(
    context: android.content.Context,
    source: Uri,
    directory: File
): SelectedPhoto {
    val mimeType = if (source.scheme == "file") "image/jpeg" else context.contentResolver.getType(source)
        ?: throw IOException("The selected file type could not be determined.")
    if (mimeType !in setOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")) {
        throw IOException("Choose a JPEG, PNG, WebP, or HEIF image.")
    }

    val sourceSize = if (source.scheme == "file") {
        source.path?.let(::File)?.length()
    } else {
        context.contentResolver.query(source, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0).takeIf { it >= 0L } else null
            }
    }
    if (sourceSize != null && sourceSize > MAX_SOURCE_BYTES) {
        throw IOException("Each selected photo must be 20 MB or smaller.")
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(source)?.use {
        BitmapFactory.decodeStream(it, null, bounds)
    } ?: throw IOException("The selected photo could not be opened.")
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        throw IOException("The selected file is not a supported image.")
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, 1920)
    }
    val decoded = context.contentResolver.openInputStream(source)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: throw IOException("The selected photo could not be decoded.")

    val oriented = orientBitmap(context, source, decoded)
    val compressed = ByteArrayOutputStream().use { output ->
        var quality = 88
        do {
            output.reset()
            oriented.compress(Bitmap.CompressFormat.JPEG, quality, output)
            quality -= 8
        } while (output.size() > MAX_COMPRESSED_BYTES && quality >= 64)
        output.toByteArray()
    }
    if (oriented !== decoded) decoded.recycle()
    oriented.recycle()
    if (compressed.size > MAX_COMPRESSED_BYTES) {
        throw IOException("The photo is too large after compression. Choose a smaller image.")
    }

    directory.mkdirs()
    val file = File(directory, "${UUID.randomUUID()}.jpg")
    try {
        FileOutputStream(file).use { it.write(compressed) }
    } catch (e: IOException) {
        file.delete()
        throw IOException("The photo could not be saved on this device.", e)
    }
    val checksum = MessageDigest.getInstance("SHA-256")
        .digest(compressed)
        .joinToString("") { byte -> "%02x".format(byte) }
    return SelectedPhoto(source.toString(), file.absolutePath, checksum)
}

private fun orientBitmap(context: android.content.Context, source: Uri, bitmap: Bitmap): Bitmap {
    val orientation = context.contentResolver.openInputStream(source)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(270f)
            matrix.postScale(-1f, 1f)
        }
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun decodeSampledBitmap(file: File, maxDimension: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        }
    )
}
