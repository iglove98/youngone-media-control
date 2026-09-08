package com.youngone.mediacontrol.policy.application;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;
class PolicySigningAndCacheTest{
 @Test void signsAndVerifies(){PolicySigningService signing=new PolicySigningService("","","test-key");String signature=signing.sign("payload");assertThat(signing.verify("payload",signature)).isTrue();}
 @Test void rejectsTamperedPayload(){PolicySigningService signing=new PolicySigningService("","","test-key");assertThat(signing.verify("tampered",signing.sign("original"))).isFalse();}
}
