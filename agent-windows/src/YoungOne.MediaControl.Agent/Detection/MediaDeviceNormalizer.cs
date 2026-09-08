using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
namespace YoungOne.MediaControl.Agent.Detection;
public sealed record MediaDeviceIdentity(string MediaType,string? VendorId,string? ProductId,string? SerialHash,string DeviceInstanceId,string DisplayName);
public static partial class MediaDeviceNormalizer{
 public static MediaDeviceIdentity Normalize(string pnpDeviceId,string? pnpClass,string? displayName,string? reportedSerial=null){
  string id=(pnpDeviceId??string.Empty).Trim().ToUpperInvariant();string media=Classify(id,pnpClass,displayName);var match=VidPid().Match(id);
  string? vid=match.Success?match.Groups["vid"].Value:null;string? pid=match.Success?match.Groups["pid"].Value:null;
  string? serial=NormalizeSerial(reportedSerial)??ExtractSerial(id);string? hash=serial is null?null:Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(serial))).ToLowerInvariant();
  return new(media,vid,pid,hash,id,string.IsNullOrWhiteSpace(displayName)?id:displayName.Trim());
 }
 static string Classify(string id,string? pnpClass,string? name){string c=(pnpClass??"").ToUpperInvariant();string n=(name??"").ToUpperInvariant();if(id.StartsWith("USBSTOR\\",StringComparison.Ordinal)||c is "DISKDRIVE" or "VOLUME")return "USB_STORAGE";if(id.StartsWith("SWD\\WPDBUSENUM",StringComparison.Ordinal)||c is "WPD" or "PORTABLEDEVICE")return "PORTABLE_DEVICE";if(c is "CDROM"||n.Contains("DVD",StringComparison.Ordinal)||n.Contains("CD-ROM",StringComparison.Ordinal))return "OPTICAL_MEDIA";if(c.Contains("BLUETOOTH",StringComparison.Ordinal))return "BLUETOOTH";return "USB_DEVICE";}
 static string? ExtractSerial(string id){int slash=id.LastIndexOf('\\');if(slash<0||slash==id.Length-1)return null;string candidate=id[(slash+1)..];if(candidate.Contains('&')&&id.StartsWith("USBSTOR\\",StringComparison.Ordinal))candidate=candidate.Split('&')[0];return NormalizeSerial(candidate);}
 static string? NormalizeSerial(string? value){if(string.IsNullOrWhiteSpace(value))return null;string normalized=new(value.Trim().Where(char.IsLetterOrDigit).Select(char.ToUpperInvariant).ToArray());return normalized.Length<3?null:normalized;}
 [GeneratedRegex(@"VID_(?<vid>[0-9A-F]{4}).*PID_(?<pid>[0-9A-F]{4})",RegexOptions.CultureInvariant)]
 private static partial Regex VidPid();
}