#pragma once
#include <ntddk.h>
#include "Public.h"
YOMC_ACTION YomcEvaluateRules(_In_reads_(ruleCount) const YOMC_POLICY_RULE* rules,_In_ ULONG ruleCount,_In_ const YOMC_EVALUATE_REQUEST* request,_Out_ PULONG matchedRuleIndex);
