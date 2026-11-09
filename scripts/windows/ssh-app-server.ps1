# Windows 11 SSH shortcut to OCI Application Server
param(
    [Parameter(Mandatory=$true)]
    [string]$AppPublicIp,

    [Parameter(Mandatory=$true)]
    [string]$PrivateKeyPath,

    [string]$RemoteUser = "ubuntu"
)

ssh -i "$PrivateKeyPath" "${RemoteUser}@${AppPublicIp}"

