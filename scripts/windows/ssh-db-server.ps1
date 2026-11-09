# Windows 11 SSH shortcut to OCI Database Server
param(
    [Parameter(Mandatory=$true)]
    [string]$DbPublicIp,

    [Parameter(Mandatory=$true)]
    [string]$PrivateKeyPath,

    [string]$RemoteUser = "ubuntu"
)

ssh -i "$PrivateKeyPath" "${RemoteUser}@${DbPublicIp}"

