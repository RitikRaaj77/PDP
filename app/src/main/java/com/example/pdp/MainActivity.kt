package com.example.pdp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.compose.ui.res.painterResource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

data class Product(
    val title: String,
    val price: Double,
    val description: String,
    val thumbnail: String,
    val images: List<String>
)

data class ProductDisplay(
    val name: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val runtime: String,
    val features: List<String>,
    val totalPlaybackTime: String,
    val description: String,
    val imageUrl: String,
    val additionalImages: List<String>,
    val offerDurationSeconds: Long
)

interface ProductApi {
    @GET("products/2")
    suspend fun getProduct(): Product
}

val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://dummyjson.com/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val productApi = retrofit.create(ProductApi::class.java)

@Composable
fun ProductDetailScreen() {
    var productDisplay by remember { mutableStateOf<ProductDisplay?>(null) }
    var timeLeft by remember { mutableStateOf<Long>(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productDisplay) {
        if (productDisplay != null) {
            Log.d("ProductDetailScreen", "Product display updated: $productDisplay")
            Log.d("ProductDetailScreen", "Additional images: ${productDisplay!!.additionalImages}")
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Log.d("ProductDetailScreen", "Error message updated: $errorMessage")
        }
    }

    LaunchedEffect(Unit) {
        Log.d("ProductDetailScreen", "Starting API call to fetch product...")
        try {
            val fetchedProduct = productApi.getProduct()
            Log.d("ProductDetailScreen", "API response received: $fetchedProduct")
            Log.d("ProductDetailScreen", "Raw images list: ${fetchedProduct.images}")
            Log.d("ProductDetailScreen", "Thumbnail: ${fetchedProduct.thumbnail}")

            val filteredImages = fetchedProduct.images
                .filter { imageUrl ->
                    val normalizedImage = imageUrl.split("?")[0].trimEnd('/')
                    val normalizedThumbnail = fetchedProduct.thumbnail.split("?")[0].trimEnd('/')
                    normalizedImage != normalizedThumbnail
                }
                .take(3)

            Log.d("ProductDetailScreen", "Filtered images: $filteredImages")

            val displayProduct = ProductDisplay(
                name = fetchedProduct.title,
                originalPrice = 1299,
                discountedPrice = fetchedProduct.price.toInt(),
                runtime = "14 Hrs Runtime",
                features = listOf("Surround Sound", "USB Cable"),
                totalPlaybackTime = "100 Hours",
                description = fetchedProduct.description,
                imageUrl = fetchedProduct.thumbnail,
                additionalImages = filteredImages,
                offerDurationSeconds = 9247
            )

            productDisplay = displayProduct
            timeLeft = displayProduct.offerDurationSeconds
            Log.d("ProductDetailScreen", "Product display set successfully")
        } catch (e: Exception) {
            Log.e("ProductDetailScreen", "Error during API call: ${e.message}", e)
            errorMessage = "Failed to load product: ${e.message}"
        }
    }

    LaunchedEffect(timeLeft) {
        while (timeLeft > 0) {
            Log.d("ProductDetailScreen", "Timer ticking: $timeLeft seconds left")
            delay(1000L)
            timeLeft -= 1
        }
        Log.d("ProductDetailScreen", "Timer finished")
    }

    val hours = (timeLeft / 3600).toString().padStart(2, '0')
    val minutes = ((timeLeft % 3600) / 60).toString().padStart(2, '0')
    val seconds = (timeLeft % 60).toString().padStart(2, '0')
    val timerText = "EXPIRES IN $hours:$minutes:$seconds"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Unknown error",
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Start
            )
        } else if (productDisplay == null) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading product details...",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Start
                )
            }
        } else {
            productDisplay?.let {
                Column(
                    modifier = Modifier
                        .background(Color.LightGray, RoundedCornerShape(16.dp))
                        .wrapContentSize()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = it.imageUrl,
                            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                            error = painterResource(android.R.drawable.ic_menu_report_image)
                        ),
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                    )

                    Text(
                        text = timerText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = it.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${it.originalPrice}",
                        fontSize = 20.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${it.discountedPrice}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow,
                        textAlign = TextAlign.Start,
                        style = LocalTextStyle.current.copy(
                            drawStyle = Stroke(
                                width = 2f,
                                cap = StrokeCap.Round,
                                miter = 10f
                            ),
                            color = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• ${it.runtime}",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start
                )
                it.features.forEach { feature ->
                    Text(
                        text = "• $feature",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "About the product:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = it.description,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (it.additionalImages.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val topImagePainter = rememberAsyncImagePainter(
                            model = it.additionalImages[0],
                            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                            error = painterResource(android.R.drawable.ic_menu_report_image)
                        )
                        Image(
                            painter = topImagePainter,
                            contentDescription = "Additional Image 1",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Gray)
                        )
                        LaunchedEffect(topImagePainter.state) {
                            if (topImagePainter.state is AsyncImagePainter.State.Error) {
                                Log.e("ProductDetailScreen", "Failed to load top image: ${it.additionalImages[0]}")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (it.additionalImages.size >= 2) {
                                val bottomLeftPainter = rememberAsyncImagePainter(
                                    model = it.additionalImages[1],
                                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                    error = painterResource(android.R.drawable.ic_menu_report_image)
                                )
                                Image(
                                    painter = bottomLeftPainter,
                                    contentDescription = "Additional Image 2",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Gray)
                                )
                                LaunchedEffect(bottomLeftPainter.state) {
                                    if (bottomLeftPainter.state is AsyncImagePainter.State.Error) {
                                        Log.e("ProductDetailScreen", "Failed to load bottom left image: ${it.additionalImages[1]}")
                                    }
                                }
                            }
                            if (it.additionalImages.size >= 3) {
                                val bottomRightPainter = rememberAsyncImagePainter(
                                    model = it.additionalImages[2],
                                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                    error = painterResource(android.R.drawable.ic_menu_report_image)
                                )
                                Image(
                                    painter = bottomRightPainter,
                                    contentDescription = "Additional Image 3",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Gray)
                                )
                                LaunchedEffect(bottomRightPainter.state) {
                                    if (bottomRightPainter.state is AsyncImagePainter.State.Error) {
                                        Log.e("ProductDetailScreen", "Failed to load bottom right image: ${it.additionalImages[2]}")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TOTAL PLAYBACK TIME",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = it.totalPlaybackTime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = it.description,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(
                            text = "ADD TO CART",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                    ) {
                        Text(
                            text = "BUY NOW",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ProductDetailScreen()
            }
        }
    }
}