#include "PolicyEvaluator.h"
static BOOLEAN BytesEqual(const UCHAR* left,const UCHAR* right,SIZE_T count){return RtlCompareMemory(left,right,count)==count;}
YOMC_ACTION YomcEvaluateRules(const YOMC_POLICY_RULE* rules,ULONG ruleCount,const YOMC_EVALUATE_REQUEST* request,PULONG matchedRuleIndex){
 LONG best=-1;ULONG bestSpecificity=0;*matchedRuleIndex=MAXULONG;
 for(ULONG i=0;i<ruleCount;i++){const YOMC_POLICY_RULE* rule=&rules[i];if(rule->MediaType!=request->MediaType)continue;if((rule->SelectorFlags&YOMC_SELECTOR_VENDOR)&&rule->VendorId!=request->VendorId)continue;if((rule->SelectorFlags&YOMC_SELECTOR_PRODUCT)&&rule->ProductId!=request->ProductId)continue;if((rule->SelectorFlags&YOMC_SELECTOR_SERIAL)&&!BytesEqual(rule->SerialHash,request->SerialHash,32))continue;ULONG specificity=((rule->SelectorFlags&YOMC_SELECTOR_VENDOR)?1:0)+((rule->SelectorFlags&YOMC_SELECTOR_PRODUCT)?1:0)+((rule->SelectorFlags&YOMC_SELECTOR_SERIAL)?1:0);if(best<0||specificity>bestSpecificity){best=(LONG)i;bestSpecificity=specificity;}}
 if(best<0)return YomcActionDetectOnly;*matchedRuleIndex=(ULONG)best;const YOMC_POLICY_RULE* selected=&rules[best];switch(request->Operation){case YomcOperationWrite:return(YOMC_ACTION)selected->WriteAction;case YomcOperationExecute:return(YOMC_ACTION)selected->ExecuteAction;default:return(YOMC_ACTION)selected->ReadAction;}
}
