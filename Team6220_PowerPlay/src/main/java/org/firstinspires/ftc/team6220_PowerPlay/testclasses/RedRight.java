package org.firstinspires.ftc.team6220_PowerPlay.testclasses;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.team6220_PowerPlay.AutoFramework;

@Autonomous(name = "RedRight")
public class RedRight extends AutoFramework {

    @Override
    public void runOpMode() throws InterruptedException {
        runAuto(AutoState.RightAutos);
    }
    // If you want to add anything to this autonomous method, make the changes in the AutoFramework method please!
}