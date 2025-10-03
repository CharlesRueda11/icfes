package com.charlesdev.icfes.student.components



import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charlesdev.icfes.student.logout.InstitutionData
import com.charlesdev.icfes.student.utils.InstitutionHelper
import com.charlesdev.icfes.student.utils.InstitutionSuggestion
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartInstitutionField(
    institutionData: InstitutionData,
    onInstitutionChange: (InstitutionData) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val institutionHelper = remember { InstitutionHelper() }

    var isExpanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<InstitutionSuggestion>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(institutionData.name) }

    // Efecto para búsqueda automática con híbrida
    LaunchedEffect(searchText) {
        if (searchText.length >= 3) {
            isSearching = true
            delay(800) // Debounce

            scope.launch {
                try {
                    // Usar búsqueda híbrida (IA + local)
                    val results = institutionHelper.hybridSearch(searchText)
                    suggestions = results
                    isExpanded = results.isNotEmpty()
                } catch (e: Exception) {
                    // Si todo falla, usar solo datos locales
                    suggestions = institutionHelper.getCommonInstitutions(searchText)
                    isExpanded = suggestions.isNotEmpty()
                } finally {
                    isSearching = false
                }
            }
        } else {
            suggestions = emptyList()
            isExpanded = false
            isSearching = false
        }
    }

    Column(modifier = modifier) {
        // Campo principal de institución
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { newValue ->
                    searchText = newValue
                    onInstitutionChange(
                        institutionData.copy(
                            name = newValue,
                            isValidated = false
                        )
                    )
                },
                label = { Text("Institución educativa") },
                placeholder = { Text("Escribe el nombre de tu colegio...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = {
                    Icon(Icons.Default.School, contentDescription = null)
                },
                trailingIcon = {
                    Row {
                        // Indicador de búsqueda
                        AnimatedVisibility(
                            visible = isSearching,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        // Indicador de validación
                        AnimatedVisibility(
                            visible = institutionData.isValidated,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Validado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (institutionData.isValidated)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                enabled = isEnabled
            )

            // Menu de sugerencias
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                if (suggestions.isEmpty() && !isSearching) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "No se encontraron instituciones",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { }
                    )
                } else {
                    suggestions.forEach { suggestion ->
                        InstitutionSuggestionItem(
                            suggestion = suggestion,
                            onClick = {
                                searchText = suggestion.fullName
                                onInstitutionChange(
                                    InstitutionData(
                                        name = suggestion.fullName,
                                        municipality = suggestion.municipality,
                                        department = suggestion.department,
                                        sector = suggestion.sector,
                                        level = suggestion.level,
                                        isValidated = true
                                    )
                                )
                                isExpanded = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }
        }

        // Campos de ubicación que se llenan automáticamente
        AnimatedVisibility(
            visible = institutionData.municipality.isNotEmpty() || institutionData.department.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campo de municipio
                    OutlinedTextField(
                        value = institutionData.municipality,
                        onValueChange = { },
                        label = { Text("Municipio") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false
                    )

                    // Campo de departamento
                    OutlinedTextField(
                        value = institutionData.department,
                        onValueChange = { },
                        label = { Text("Departamento") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false
                    )
                }

                // Información adicional
                if (institutionData.sector.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sector: ${institutionData.sector}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                                if (institutionData.level.isNotEmpty()) {
                                    Text(
                                        text = "Nivel: ${institutionData.level}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón para validar manualmente
        AnimatedVisibility(
            visible = institutionData.name.isNotEmpty() && !institutionData.isValidated && suggestions.isEmpty() && !isSearching,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        isSearching = true
                        try {
                            // Usar validación híbrida
                            val validated = institutionHelper.hybridValidation(
                                institutionName = institutionData.name
                            )

                            if (validated != null) {
                                onInstitutionChange(
                                    InstitutionData(
                                        name = validated.fullName,
                                        municipality = validated.municipality,
                                        department = validated.department,
                                        sector = validated.sector,
                                        level = validated.level,
                                        isValidated = true
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            // Mantener el texto actual pero marcar como no validado
                        } finally {
                            isSearching = false
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Validar y completar información")
            }
        }
    }
}

@Composable
private fun InstitutionSuggestionItem(
    suggestion: InstitutionSuggestion,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Column {
                Text(
                    text = suggestion.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${suggestion.municipality}, ${suggestion.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = suggestion.sector,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                tint = when (suggestion.sector) {
                    "Público" -> MaterialTheme.colorScheme.primary
                    "Privado" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    )
}