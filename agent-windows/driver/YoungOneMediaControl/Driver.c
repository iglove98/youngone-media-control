#include <ntddk.h>
#include <wdf.h>
#include <wdmsec.h>
#include <bcrypt.h>
#include "Public.h"
#include "PolicyEvaluator.h"
#pragma comment(lib,"Cng.lib")
DRIVER_INITIALIZE DriverEntry;
EVT_WDF_DRIVER_DEVICE_ADD YomcEvtDeviceAdd;
EVT_WDF_IO_QUEUE_IO_DEVICE_CONTROL YomcEvtIoDeviceControl;
EVT_WDF_DRIVER_UNLOAD YomcEvtDriverUnload;
static YOMC_DRIVER_STATUS g_Status={YOMC_POLICY_MAGIC,YOMC_ABI_VERSION,0,0,FALSE,FALSE,FALSE,STATUS_NOT_SUPPORTED};
static PYOMC_POLICY_RULE g_Rules=NULL;
static ULONG g_RuleCount=0;
static WDFWAITLOCK g_PolicyLock=NULL;
static NTSTATUS VerifyPayloadHash(const UCHAR* payload,ULONG length,const UCHAR expected[32]){
 UCHAR actual[32];NTSTATUS status=BCryptHash(BCRYPT_SHA256_ALG_HANDLE,NULL,0,(PUCHAR)payload,length,actual,sizeof(actual));if(!NT_SUCCESS(status))return status;return RtlCompareMemory(actual,expected,sizeof(actual))==sizeof(actual)?STATUS_SUCCESS:STATUS_DATA_ERROR;
}
NTSTATUS DriverEntry(PDRIVER_OBJECT driverObject,PUNICODE_STRING registryPath){WDF_DRIVER_CONFIG config;WDF_OBJECT_ATTRIBUTES attributes;WDFDRIVER driver;WDF_DRIVER_CONFIG_INIT(&config,YomcEvtDeviceAdd);config.EvtDriverUnload=YomcEvtDriverUnload;NTSTATUS status=WdfDriverCreate(driverObject,registryPath,WDF_NO_OBJECT_ATTRIBUTES,&config,&driver);if(!NT_SUCCESS(status))return status;WDF_OBJECT_ATTRIBUTES_INIT(&attributes);attributes.ParentObject=driver;return WdfWaitLockCreate(&attributes,&g_PolicyLock);}
VOID YomcEvtDriverUnload(WDFDRIVER driver){UNREFERENCED_PARAMETER(driver);if(g_PolicyLock!=NULL)WdfWaitLockAcquire(g_PolicyLock,NULL);PYOMC_POLICY_RULE old=g_Rules;g_Rules=NULL;g_RuleCount=0;if(g_PolicyLock!=NULL)WdfWaitLockRelease(g_PolicyLock);if(old!=NULL)ExFreePoolWithTag(old,'cMoY');}
NTSTATUS YomcEvtDeviceAdd(WDFDRIVER driver,PWDFDEVICE_INIT init){
 UNREFERENCED_PARAMETER(driver);WDFDEVICE device;WDF_IO_QUEUE_CONFIG queue;NTSTATUS status;DECLARE_CONST_UNICODE_STRING(sddl,L"D:P(A;;GA;;;SY)(A;;GA;;;BA)");DECLARE_CONST_UNICODE_STRING(symbolicLink,L"\\DosDevices\\YoungOneMediaControl");
 WdfDeviceInitSetDeviceType(init,YOMC_DEVICE_TYPE);status=WdfDeviceInitAssignSDDLString(init,&sddl);if(!NT_SUCCESS(status))return status;status=WdfDeviceCreate(&init,WDF_NO_OBJECT_ATTRIBUTES,&device);if(!NT_SUCCESS(status))return status;status=WdfDeviceCreateSymbolicLink(device,&symbolicLink);if(!NT_SUCCESS(status))return status;WDF_IO_QUEUE_CONFIG_INIT_DEFAULT_QUEUE(&queue,WdfIoQueueDispatchSequential);queue.EvtIoDeviceControl=YomcEvtIoDeviceControl;return WdfIoQueueCreate(device,&queue,WDF_NO_OBJECT_ATTRIBUTES,WDF_NO_HANDLE);
}
static NTSTATUS ValidatePolicy(WDFREQUEST request,size_t inputLength,PYOMC_POLICY_HEADER* header){
 NTSTATUS status;if(inputLength<sizeof(YOMC_POLICY_HEADER))return STATUS_BUFFER_TOO_SMALL;status=WdfRequestRetrieveInputBuffer(request,sizeof(YOMC_POLICY_HEADER),(PVOID*)header,NULL);if(!NT_SUCCESS(status))return status;if((*header)->Magic!=YOMC_POLICY_MAGIC||(*header)->AbiVersion!=YOMC_ABI_VERSION)return STATUS_REVISION_MISMATCH;if((*header)->PayloadLength>YOMC_MAX_POLICY_BYTES)return STATUS_FILE_TOO_LARGE;if((*header)->PayloadLength!=inputLength-sizeof(YOMC_POLICY_HEADER))return STATUS_INFO_LENGTH_MISMATCH;if((*header)->RuleCount==0||(*header)->RuleCount>YOMC_MAX_POLICY_BYTES/YOMC_RULE_BYTES)return STATUS_INVALID_PARAMETER;if((*header)->PayloadLength!=(*header)->RuleCount*YOMC_RULE_BYTES)return STATUS_INFO_LENGTH_MISMATCH;return VerifyPayloadHash((const UCHAR*)(*header+1),(*header)->PayloadLength,(*header)->PayloadHash);
}
static NTSTATUS InstallPolicy(const YOMC_POLICY_HEADER* header){
 SIZE_T bytes=header->PayloadLength;PYOMC_POLICY_RULE replacement=(PYOMC_POLICY_RULE)ExAllocatePool2(POOL_FLAG_NON_PAGED,bytes,'cMoY');if(replacement==NULL)return STATUS_INSUFFICIENT_RESOURCES;RtlCopyMemory(replacement,header+1,bytes);
 for(ULONG i=0;i<header->RuleCount;i++){if(replacement[i].MediaType<1||replacement[i].MediaType>4||replacement[i].ReadAction<1||replacement[i].ReadAction>4||replacement[i].WriteAction<1||replacement[i].WriteAction>4||replacement[i].ExecuteAction<1||replacement[i].ExecuteAction>4){ExFreePoolWithTag(replacement,'cMoY');return STATUS_INVALID_PARAMETER;}}
 WdfWaitLockAcquire(g_PolicyLock,NULL);PYOMC_POLICY_RULE old=g_Rules;g_Rules=replacement;g_RuleCount=header->RuleCount;g_Status.AcceptedPolicyVersion=header->PolicyVersion;g_Status.AcceptedRuleCount=header->RuleCount;g_Status.PolicyLoaded=TRUE;g_Status.EvaluatorReady=TRUE;WdfWaitLockRelease(g_PolicyLock);if(old!=NULL)ExFreePoolWithTag(old,'cMoY');return STATUS_SUCCESS;
}
VOID YomcEvtIoDeviceControl(WDFQUEUE queue,WDFREQUEST request,size_t outputLength,size_t inputLength,ULONG code){
 UNREFERENCED_PARAMETER(queue);UNREFERENCED_PARAMETER(outputLength);NTSTATUS status=STATUS_INVALID_DEVICE_REQUEST;size_t written=0;
 if(code==IOCTL_YOMC_QUERY_STATUS){PYOMC_DRIVER_STATUS out=NULL;status=WdfRequestRetrieveOutputBuffer(request,sizeof(YOMC_DRIVER_STATUS),(PVOID*)&out,NULL);if(NT_SUCCESS(status)){WdfWaitLockAcquire(g_PolicyLock,NULL);RtlCopyMemory(out,&g_Status,sizeof(g_Status));WdfWaitLockRelease(g_PolicyLock);written=sizeof(g_Status);}}
 else if(code==IOCTL_YOMC_SET_POLICY){PYOMC_POLICY_HEADER header=NULL;status=ValidatePolicy(request,inputLength,&header);if(NT_SUCCESS(status))status=InstallPolicy(header);g_Status.LastPolicyStatus=status;}
 else if(code==IOCTL_YOMC_EVALUATE){PYOMC_EVALUATE_REQUEST in=NULL;PYOMC_EVALUATE_RESULT out=NULL;status=WdfRequestRetrieveInputBuffer(request,sizeof(YOMC_EVALUATE_REQUEST),(PVOID*)&in,NULL);if(NT_SUCCESS(status))status=WdfRequestRetrieveOutputBuffer(request,sizeof(YOMC_EVALUATE_RESULT),(PVOID*)&out,NULL);if(NT_SUCCESS(status)){WdfWaitLockAcquire(g_PolicyLock,NULL);if(g_Rules==NULL){status=STATUS_DEVICE_NOT_READY;}else{out->Action=YomcEvaluateRules(g_Rules,g_RuleCount,in,&out->MatchedRuleIndex);out->Matched=out->MatchedRuleIndex!=MAXULONG;out->PolicyVersion=g_Status.AcceptedPolicyVersion;written=sizeof(*out);}WdfWaitLockRelease(g_PolicyLock);}}
 WdfRequestCompleteWithInformation(request,status,written);
}
