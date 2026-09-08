using Xunit;
using YoungOne.MediaControl.Agent.Detection;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class MediaDeviceNormalizerTests{
 [Fact] public void ParsesUsbVidPidAndSerial(){var d=MediaDeviceNormalizer.Normalize(@"USB\VID_0781&PID_5583\4C530001230101117143","USB","Flash Disk");Assert.Equal("0781",d.VendorId);Assert.Equal("5583",d.ProductId);Assert.Equal("USB_DEVICE",d.MediaType);Assert.NotNull(d.SerialHash);}
 [Fact] public void ClassifiesUsbStorage(){var d=MediaDeviceNormalizer.Normalize(@"USBSTOR\DISK&VEN_SANDISK&PROD_ULTRA\4C530001230101117143&0","DiskDrive","SanDisk Ultra");Assert.Equal("USB_STORAGE",d.MediaType);Assert.NotNull(d.SerialHash);}
 [Fact] public void ClassifiesPortableDevice(){var d=MediaDeviceNormalizer.Normalize(@"SWD\WPDBUSENUM\_??_USB#VID_04E8&PID_6860#PHONE","WPD","Phone");Assert.Equal("PORTABLE_DEVICE",d.MediaType);}
 [Fact] public void SerialHashIsStableAndRawSerialIsNotExposed(){var a=MediaDeviceNormalizer.Normalize(@"USB\VID_1234&PID_ABCD\Serial-001","USB","Device");var b=MediaDeviceNormalizer.Normalize(@"usb\vid_1234&pid_abcd\serial001","USB","Device");Assert.Equal(a.SerialHash,b.SerialHash);Assert.DoesNotContain("SERIAL",a.SerialHash!,StringComparison.OrdinalIgnoreCase);}
}