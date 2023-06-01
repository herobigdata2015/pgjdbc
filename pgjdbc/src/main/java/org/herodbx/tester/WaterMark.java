package org.herodbx.tester;

import org.herodbx.util.HeroUtils;

/**
 * 按照内置的逻辑生成jdbc驱动的水印。
 *
 * @author 顾法华
 * @since 2020年6月1日
 */
public class WaterMark {
  static public void main(String[] args){
    System.out.println("WaterMark is: "+new String(HeroUtils.getWaterMark()));
  }
}
