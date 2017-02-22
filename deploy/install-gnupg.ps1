$url = "https://ulrik.is/gnupg2.zip"
$output = "C:\gnupg2.zip"
$shahash = "31543c90427351ea03d97e5c115c9e9dc810ad41"

Add-Type -AssemblyName System.IO.Compression.FileSystem
function Unzip
{
    param([string]$zipfile, [string]$outpath)

    [System.IO.Compression.ZipFile]::ExtractToDirectory($zipfile, $outpath)
}

Invoke-WebRequest -Uri $url -OutFile $output

$actual_hash = (Get-FileHash $output -Algorithm SHA1) | Select-Object -ExpandProperty Hash

if($actual_hash -eq $shahash) {
        Write-Output "Installing gpg4win..."
        Unzip "$output" "C:\"
        Write-Output "Installation done."
        exit 0
} else {
        Write-Output "SHA checksum failed: $actual_hash vs $shahash"
        exit 1
}
