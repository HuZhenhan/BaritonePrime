param(
    [Parameter(Mandatory=$true)]
    [string] $Path
)

$sr = New-Object System.IO.StreamReader($Path)
$txt = $sr.ReadToEnd()
$sr.Close()

# UTF-8 without BOM
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($Path, $txt, $enc)

