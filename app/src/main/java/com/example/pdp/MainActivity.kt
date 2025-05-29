package com.example.pdp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
import kotlin.math.tan
import kotlin.random.Random
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(modifier: Modifier = Modifier, onTimeout: () -> Unit) {
    val i = 15
    val totalAnimationDuration = 2000L
    val fadeOutDuration = 1000L
    val fallDuration = 1000

    val blueShades = List(i) { index ->
        val intensity = 0xFF - (index * 20)
        Color(0xFF1565C0 + (intensity shl 16))
    }

    val lineOffsets = List(i) { remember { Animatable(-1000f) } }
    val lineAlphas = List(i) { remember { Animatable(0f) } }
    val lineXPositions = List(i) { Random.nextInt(-200, 200).toFloat() }
    val textScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    var shouldFadeOut by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }

    val glowEffect = rememberInfiniteTransition()
    val glow by glowEffect.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(totalAnimationDuration - fadeOutDuration)
        shouldFadeOut = true
        delay(fadeOutDuration)
        shouldNavigate = true
    }

    LaunchedEffect(shouldFadeOut) {
        lineOffsets.forEachIndexed { index, offset ->
            launch {
                while (!shouldFadeOut) {
                    delay(Random.nextLong(0, 1500))
                    offset.snapTo(-1000f)
                    offset.animateTo(
                        targetValue = 1000f,
                        animationSpec = tween(
                            durationMillis = fallDuration,
                            easing = LinearEasing
                        )
                    )
                }
                lineAlphas[index].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(1000, easing = LinearEasing)
                )
            }
            launch {
                while (!shouldFadeOut) {
                    delay(Random.nextLong(0, 1500))
                    lineAlphas[index].snapTo(0f)
                    lineAlphas[index].animateTo(
                        targetValue = 0.5f,
                        animationSpec = tween(600, easing = LinearEasing)
                    )
                }
            }
        }

        textScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
    }

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            onTimeout()
        }
    }

    val offWhite = Color(0xFFCBCBCB)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        lineOffsets.forEachIndexed { index, offset ->
            Box(
                modifier = Modifier
                    .offset(x = lineXPositions[index].dp, y = offset.value.dp)
                    .width(10.dp)
                    .height(2000.dp)
                    .background(blueShades[index])
                    .alpha(lineAlphas[index].value)
            )
        }

        Text(
            text = "",
            color = offWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .scale(textScale.value)
                .alpha(textAlpha.value * if (shouldFadeOut) 1f - (textAlpha.value * glow) else glow)
        )
    }
}

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

            val additionalImages = listOf(
                "https://images.pexels.com/photos/205923/pexels-photo-205923.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/1327689/pexels-photo-1327689.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/2639947/pexels-photo-2639947.jpeg?auto=compress&cs=tinysrgb&w=600"
            )

            Log.d("ProductDetailScreen", "Using hardcoded images: $additionalImages")

            val displayProduct = ProductDisplay(
                name = fetchedProduct.title,
                originalPrice = 1299,
                discountedPrice = fetchedProduct.price.toInt(),
                runtime = "14 Hrs Runtime",
                features = listOf("Surround Sound", "USB Cable"),
                totalPlaybackTime = "100 Hours",
                description = fetchedProduct.description,
                imageUrl = fetchedProduct.thumbnail,
                additionalImages = additionalImages,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.navigationBars)) // Add padding for both status and navigation bars
            .padding(horizontal = 16.dp, vertical = 10.dp) // Add 10.dp buffer at top and bottom, retain horizontal padding
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading product details...",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                productDisplay?.let {
                    Column(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                            .background(Color.LightGray, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 32.dp, bottomEnd = 32.dp))
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
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(45.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(190.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(
                                Color.Red,
                                RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                            )
                    ) {
                        Text(
                            text = timerText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
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
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .drawBehind {
                                    val lineWidth = 6f
                                    val angleRad = Math.toRadians(22.0)
                                    val startX = 0f
                                    val startY = size.height.toFloat()
                                    val endX = size.width.toFloat()
                                    val endY = (size.height - tan(angleRad) * size.width).toFloat()

                                    drawLine(
                                        color = Color.Red,
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = lineWidth
                                    )
                                }
                        ) {
                            Text(
                                text = "₹${it.originalPrice}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }

                        Box {
                            Text(
                                text = "₹${it.discountedPrice}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = LocalTextStyle.current.copy(
                                    drawStyle = Stroke(width = 8f),
                                    color = Color.Black
                                )
                            )

                            Text(
                                text = "₹${it.discountedPrice}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Yellow
                            )
                        }
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = it.description,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (it.additionalImages.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
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
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Gray),
                                contentScale = ContentScale.FillBounds
                            )
                            LaunchedEffect(topImagePainter.state) {
                                if (topImagePainter.state is AsyncImagePainter.State.Error) {
                                    Log.e("ProductDetailScreen", "Failed to load top image: ${it.additionalImages[0]}")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
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
                                            .weight(1f)
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.FillBounds
                                    )
                                    LaunchedEffect(bottomLeftPainter.state) {
                                        if (bottomLeftPainter.state is AsyncImagePainter.State.Error) {
                                            Log.e("ProductDetailScreen", "Failed to load bottom left image: ${it.additionalImages[1]}")
                                        }
                                    }
                                }
                                if (it.additionalImages.size >= 3) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val bottomRightPainter = rememberAsyncImagePainter(
                                        model = it.additionalImages[2],
                                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                        error = painterResource(android.R.drawable.ic_menu_report_image)
                                    )
                                    Image(
                                        painter = bottomRightPainter,
                                        contentDescription = "Additional Image 3",
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.FillBounds
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = it.description,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val features = listOf(
                                Triple(Icons.Default.Star, "Genuine\nProducts", null),
                                Triple(null, "Limited Time\nDeals", R.drawable.timer),
                                Triple(null, "Free\nShipping", R.drawable.localshipping),
                                Triple(Icons.Default.Lock, "Secure\nPayments", null)
                            )

                            features.forEach { (vector, label, drawable) ->
                                Column(
                                    modifier = Modifier
                                        .width(IntrinsicSize.Max)
                                        .widthIn(max = 70.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (vector != null) {
                                        Icon(
                                            imageVector = vector,
                                            contentDescription = label,
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (drawable != null) {
                                        Icon(
                                            painter = painterResource(id = drawable),
                                            contentDescription = label,
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        listOf(
                            Triple("ADD TO CART", Color.White, Color.Black),
                            Triple("BUY NOW", Color(0xFFFF4081), Color.White)
                        ).forEach { (text, bgColor, textColor) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 4.dp, end = 2.dp)
                                    .background(Color.Black, RoundedCornerShape(6.dp))
                            ) {
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = (-4).dp, x = (-3).dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                                ) {
                                    Text(
                                        text = text,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .padding(top = 10.dp)
                .align(Alignment.TopStart)
                .size(27.dp)
                .clip(CircleShape)
                .border(3.5.dp, Color.Black, CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.WHITE
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true

        setContent {
            MaterialTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onTimeout = {
                            showSplash = false
                        }
                    )
                } else {
                    ProductDetailScreen()
                }
            }
        }
    }
}