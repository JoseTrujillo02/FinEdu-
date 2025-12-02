@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// DIÁLOGO: Capital No Configurado
@Composable
fun CapitalRequiredDialog(
    colors: AppColors,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Capital No Configurado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Para poder registrar transacciones, primero debes configurar tu capital inicial en tu perfil de usuario.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Ve a tu perfil y configura tu capital inicial antes de comenzar a registrar ingresos o gastos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text(
                        "Entendido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// BARRA DE BÚSQUEDA
@Composable
fun SearchBar(
    colors: AppColors,
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                "Buscar por categoría, descripción o monto...",
                color = colors.textTertiary,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Buscar",
                tint = colors.textTertiary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Limpiar",
                        tint = colors.textTertiary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = colors.textTertiary.copy(alpha = 0.2f),
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.primary
        )
    )
}

// FILTROS Y ORDENAMIENTO
@Composable
fun FilterAndSortRow(
    colors: AppColors,
    currentFilter: TransactionFilter,
    currentSort: TransactionSort,
    onFilterChange: (TransactionFilter) -> Unit,
    onSortChange: (TransactionSort) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    colors = colors,
                    label = "Todas",
                    icon = Icons.AutoMirrored.Outlined.List,
                    isSelected = currentFilter == TransactionFilter.ALL,
                    onClick = { onFilterChange(TransactionFilter.ALL) }
                )
            }
            item {
                FilterChip(
                    colors = colors,
                    label = "Ingresos",
                    icon = Icons.Outlined.ArrowUpward,
                    isSelected = currentFilter == TransactionFilter.INCOME,
                    onClick = { onFilterChange(TransactionFilter.INCOME) },
                    chipColor = colors.primary
                )
            }
            item {
                FilterChip(
                    colors = colors,
                    label = "Egresos",
                    icon = Icons.Outlined.ArrowDownward,
                    isSelected = currentFilter == TransactionFilter.EXPENSE,
                    onClick = { onFilterChange(TransactionFilter.EXPENSE) },
                    chipColor = colors.expense
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ordenar por:",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.primary.copy(alpha = 0.05f),
                        contentColor = colors.primary
                    )
                ) {
                    Text(
                        text = when (currentSort) {
                            TransactionSort.DATE_DESC -> "Más recientes"
                            TransactionSort.DATE_ASC -> "Más antiguos"
                            TransactionSort.AMOUNT_DESC -> "Mayor monto"
                            TransactionSort.AMOUNT_ASC -> "Menor monto"
                            TransactionSort.CATEGORY -> "Categoría"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    SortMenuItem(
                        colors,
                        "Más recientes",
                        Icons.Outlined.ArrowDownward,
                        currentSort == TransactionSort.DATE_DESC
                    ) {
                        onSortChange(TransactionSort.DATE_DESC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Más antiguos",
                        Icons.Outlined.ArrowUpward,
                        currentSort == TransactionSort.DATE_ASC
                    ) {
                        onSortChange(TransactionSort.DATE_ASC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Mayor monto",
                        Icons.AutoMirrored.Outlined.TrendingUp,
                        currentSort == TransactionSort.AMOUNT_DESC
                    ) {
                        onSortChange(TransactionSort.AMOUNT_DESC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Menor monto",
                        Icons.AutoMirrored.Outlined.TrendingDown,
                        currentSort == TransactionSort.AMOUNT_ASC
                    ) {
                        onSortChange(TransactionSort.AMOUNT_ASC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Categoría",
                        Icons.Outlined.Category,
                        currentSort == TransactionSort.CATEGORY
                    ) {
                        onSortChange(TransactionSort.CATEGORY)
                        showSortMenu = false
                    }
                }
            }
        }
    }
}

@Composable
fun SortMenuItem(
    colors: AppColors,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) colors.primary else colors.textSecondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    label,
                    color = if (isSelected) colors.primary else colors.textPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        onClick = onClick
    )
}

@Composable
fun FilterChip(
    colors: AppColors,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    chipColor: Color = colors.primary
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isSelected) chipColor.copy(alpha = 0.15f) else colors.surfaceVariant,
            labelColor = if (isSelected) chipColor else colors.textSecondary,
            iconColor = if (isSelected) chipColor else colors.textSecondary,
            selectedContainerColor = chipColor.copy(alpha = 0.15f),
            selectedLabelColor = chipColor,
            selectedLeadingIconColor = chipColor
        ),
        border = if (isSelected) {
            BorderStroke(1.5.dp, chipColor.copy(alpha = 0.5f))
        } else {
            BorderStroke(1.dp, colors.textTertiary.copy(alpha = 0.2f))
        }
    )
}

// FILTRO POR CATEGORÍA
@Composable
fun CategoryFilterRow(
    colors: AppColors,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtrar por categoría:",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            if (selectedCategory != null) {
                TextButton(
                    onClick = { onCategorySelected(selectedCategory) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Limpiar filtro",
                        modifier = Modifier.size(16.dp),
                        tint = colors.textTertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Limpiar",
                        fontSize = 13.sp,
                        color = colors.textTertiary
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val isSelected = selectedCategory == category

                val categoryIcon = when (category.lowercase()) {
                    "comida", "alimentos" -> Icons.Outlined.Restaurant
                    "transporte" -> Icons.Outlined.DirectionsCar
                    "entretenimiento" -> Icons.Outlined.Theaters
                    "salud" -> Icons.Outlined.LocalHospital
                    "educación" -> Icons.Outlined.School
                    "ropa" -> Icons.Outlined.Checkroom
                    "servicios" -> Icons.Outlined.Build
                    "hogar" -> Icons.Outlined.Home
                    else -> Icons.Outlined.Category
                }

                FilterChip(
                    colors = colors,
                    label = category,
                    icon = categoryIcon,
                    isSelected = isSelected,
                    onClick = { onCategorySelected(category) },
                    chipColor = Color(0xFF9C27B0)
                )
            }
        }
    }
}