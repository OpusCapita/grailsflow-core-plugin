import com.jcatalog.grailsflow.utils.ConstantUtils

class SleepTestProcess {

    public Integer var1
    public Integer var2
    public Integer var3
    public Integer var4
    public Integer var5
    public String str1
    public String str2
    public String str3
    public String str4
    public String str5
    
    def descriptions = {
        SleepTest(description_en : "description..." )
    }
    
    def views = {
    }
    
    def SleepTestProcess = {
        initialize() {
            action {
                Log(logMessage: "Sleep test process has been started.")
                var1 = 1001
                var2 = 2001
                var3 = 3001
                var4 = 4001
                var5 = 5001
                str1 = "val1"
                str2 = "val2"
                str3 = "val3"
                str4 = "val4"
                str5 = "val5"
                return Sleep(ms: 500)
            }
            on("okay").to([ "step1" ])
        }
        
        step1() {
            action {
                Log(logMessage: "Executiong step1...")
                var1 = 1002
                var2 = 2002
                var3 = 3002
                var4 = 4002
                var5 = 5002
                str1 = "val10"
                str2 = "val20"
                str3 = "val30"
                str4 = "val40"
                str5 = "val50"
                Sleep(ms: 3000)
                println(".................!!!STILL IN ACTION!!!")
                return "okay"
            }
            on("okay").to(["step2"])
        }
        
        step2() {
            action {
                Log(logMessage: "Executiong step2...")
                var1 = 1003
                var2 = 2003
                var3 = 3003
                var4 = 4003
                var5 = 5003
                str1 = "val100"
                str2 = "val200"
                str3 = "val300"
                str4 = "val400"
                str5 = "val500"
                return Sleep(ms: 1000)
            }
            on("okay").to(["step3"])
        }
        
        step3() {
            action {
                Log(logMessage: "Executiong step3...")
                var1 = 1004
                var2 = 2004
                var3 = 3004
                var4 = 4004
                var5 = 5004
                str1 = "val1000"
                str2 = "val2000"
                str3 = "val3000"
                str4 = "val4000"
                str5 = "val5000"
                return Sleep(ms: 750)
            }
            on("okay").to(["step4"])
        }
        
        step4() {
            action {
                Log(logMessage: "Executiong step4...")
                var1 = 1005
                var2 = 2005
                var3 = 3005
                var4 = 4005
                var5 = 5005
                str1 = "val10000"
                str2 = "val20000"
                str3 = "val30000"
                str4 = "val40000"
                str5 = "val50000"
                return Sleep(ms: 2000)
            }
            on("okay").to(["step5"])
        }
        
        step5() {
            action {
                Log(logMessage: "Executiong step5...")
                var1 = 1006
                var2 = 2006
                var3 = 3006
                var4 = 4006
                var5 = 5006
                str1 = "val100000"
                str2 = "val200000"
                str3 = "val300000"
                str4 = "val400000"
                str5 = "val500000"
                return Sleep(ms: 3000)
            }
            on("okay").to(["step6"])
        }
        
        step6() {
            action {
                Log(logMessage: "Executiong step6...")
                var1 = 1007
                var2 = 2007
                var3 = 3007
                var4 = 4007
                var5 = 5007
                str1 = "val1000000"
                str2 = "val2000000"
                str3 = "val3000000"
                str4 = "val4000000"
                str5 = "val5000000"
                return Sleep(ms: 4000)
            }
            on("okay").to(["step7"])
        }
        
        step7() {
            action {
                Log(logMessage: "Executiong step7...")
                var1 = 1008
                var2 = 2008
                var3 = 3008
                var4 = 4008
                var5 = 5008
                str1 = "val10000000"
                str2 = "val20000000"
                str3 = "val30000000"
                str4 = "val40000000"
                str5 = "val50000000"
                return Sleep(ms: 5000)
            }
            on("okay").to(["step8"])
        }
        
        step8() {
            action {
                Log(logMessage: "Executiong step8...")
                var1 = 1009
                var2 = 2009
                var3 = 3009
                var4 = 4009
                var5 = 5009
                str1 = "val100000000"
                str2 = "val200000000"
                str3 = "val300000000"
                str4 = "val400000000"
                str5 = "val500000000"
                return Sleep(ms: 2000)
            }
            on("okay").to(["step9"])
        }
        
        step9() {
            action {
                Log(logMessage: "Executiong step9...")
                var1 = 1010
                var2 = 2010
                var3 = 3010
                var4 = 4010
                var5 = 5010
                str1 = "val1000000000"
                str2 = "val2000000000"
                str3 = "val3000000000"
                str4 = "val4000000000"
                str5 = "val5000000000"
                return Sleep(ms: 3000)
            }
            on("okay").to(["finishOk"])
        }
        
        finishOk() {
            action {
                Log(logMessage: "Sleep test process has been finished.")
            }
        }
    }
}
