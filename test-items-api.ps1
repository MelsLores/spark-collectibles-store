# Script de Pruebas para la API de Artículos
# Ejecutar en PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PRUEBAS DE API - ARTÍCULOS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Función para hacer requests y mostrar resultados
function Test-Endpoint {
    param($Url, $Description)
    Write-Host "Probando: $Description" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -ContentType "application/json"
        $response | ConvertTo-Json -Depth 10
        Write-Host "✓ Exitoso" -ForegroundColor Green
    } catch {
        Write-Host "✗ Error: $_" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "----------------------------------------" -ForegroundColor Gray
    Write-Host ""
}

# Prueba 1: GET /items - Lista de artículos
Test-Endpoint -Url "http://localhost:4567/items" `
              -Description "Requerimiento 1: Lista de artículos (ID, nombre, precio)"

# Prueba 2: GET /items/item1 - Artículo específico
Test-Endpoint -Url "http://localhost:4567/items/item1" `
              -Description "Requerimiento 2: Descripción de artículo por ID (item1)"

# Prueba 3: GET /items/item3 - Otro artículo
Test-Endpoint -Url "http://localhost:4567/items/item3" `
              -Description "Descripción de artículo por ID (item3 - Chamarra Bad Bunny)"

# Prueba 4: GET /items/item5/description - Solo descripción
Test-Endpoint -Url "http://localhost:4567/items/item5/description" `
              -Description "Ruta alternativa: Solo descripción (item5)"

# Prueba 5: GET /items/invalid - Artículo inexistente
Test-Endpoint -Url "http://localhost:4567/items/invalid" `
              -Description "Prueba de error: Artículo inexistente"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FIN DE PRUEBAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
