$url = "https://files.gpg4win.org/gpg4win-vanilla-2.3.3.exe"
$output = "gpg4win-vanilla-2.3.3.exe"
$shahash = "a105cc82d60a315a14a4f69ea783a83baa434e55"

Invoke-WebRequest -Uri $url -OutFile $output

$actual_hash = (Get-FileHash $output -Algorithm SHA1) | Select-Object -ExpandProperty Hash

if($actual_hash -eq $shahash) {
        Write-Output "Installing gpg4win..."
        & .\$output /S
        Write-Output "Installation done."
        exit 0
} else {
        Write-Output "SHA checksum failed: $actual_hash vs $shahash"
        exit 1
}
