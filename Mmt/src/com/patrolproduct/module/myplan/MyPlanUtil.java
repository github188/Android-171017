package com.patrolproduct.module.myplan;

import com.mapgis.mmt.MyApplication;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class MyPlanUtil {
    /**
     * 区域巡检
     */
    public static final int PLAN_AREA_PID = 1;

    /**
     * 路径巡检
     */
    public static final int PLAN_PATH_PID = 2;

    /**
     * 设备巡检
     */
    public static final int PLAN_DEVICE_PID = 3;

    /**
     * 管线巡检
     */
    public static final int PLAN_PIPE_PID = (int) MyApplication.getInstance().getConfigValue("PipePlanID", 4);

    /**
     * 文字记录并展示
     * <p/>
     * 区域巡检不显示到位数跟反馈数
     * <p/>
     * 路径巡检不显示反馈数
     */
    public static String getTaskStateString(int taskId) {
        int[] state = getTaskState(taskId);

        /**
         int pid = -1;

         for (PatrolTask kv : SessionManager.patrolTaskList) {
         if (Integer.valueOf(kv.TaskID) == taskId) {
         pid = Integer.valueOf(kv.PlanInfo.PType.toString());
         break;
         }
         }

         String taskStateString = "";

         if (pid == MyPlanUtil.PLAN_AREA_PID) {
         taskStateString = "区域巡检地图显示";
         } else if (pid == MyPlanUtil.PLAN_PATH_PID || pid == MyPlanUtil.PLAN_PIPE_PID) {
         taskStateString = "到位: " + state[0] + ";  总数: " + state[2];
         } else {
         taskStateString = "到位: " + state[0] + ";  反馈: " + state[1] + ";  总数: " + state[2];
         }

         return taskStateString;
         **/

        return "到位: " + state[0] + ";  反馈: " + state[1] + ";  总数: " + state[2];
    }

    /**
     * 数字记录并展示
     */
    public static String getTaskStateCount(int taskId) {
        int[] state = getTaskState(taskId);

        return state[0] + "," + state[1] + "," + state[2];
    }

    /**
     * 对设备状态分类计数
     */
    public static int[] getTaskState(int taskId) {
        int arriveCount = 0, feedbackCount = 0, total = 0;

        for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
            if (Integer.valueOf(kv.get("taskId")) != taskId) {
                continue;
            }

            if (kv.get("isArrive").equals("1")) {
                arriveCount++;
            }

            if (isEuqipPlan(kv)) {
                if (kv.get("isFeedback").equals("1")) {
                    feedbackCount++;
                }
            } else {
                feedbackCount = Integer.valueOf(kv.get("feedbackTotal"));
            }

            total++;
        }

        return new int[]{arriveCount, feedbackCount, total};
    }

    public static boolean isEuqipPlan(Map<String, String> task) {
        return task.containsKey("layerName") && task.get("layerName").trim().length() != 0;
    }

    /**
     * 设备巡检更新反馈次数
     */
    public static void updateArriveOrFeedbackState(Object taskId, Object layerName, Object pipeId, Boolean isArrive) {
        for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
            if (kv.get("taskId").equals(taskId.toString()) && kv.get("layerName").equals(layerName.toString())
                    && kv.get("pipeId").equals(pipeId.toString())) {
                if (isArrive) {
                    kv.put("isArrive", "1");
                } else {
                    kv.put("isFeedback", "1");
                }

                break;
            }
        }
    }

    /**
     * 路径 区域 巡检更新反馈次数
     */
    public static void updateArriveOrFeedbackState(Object taskId) {
        for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
            if (kv.get("taskId").equals(taskId.toString())) {
                int feedbackTotal = Integer.valueOf(kv.get("feedbackTotal"));
                feedbackTotal = feedbackTotal + 1;
                kv.put("feedbackTotal", feedbackTotal + "");
            }
        }
    }

    /**
     * 将,(14325423,21331541)分割的字符串数组建立为Dot对象
     */
    public static Dots buildDots(String[] lineStrings) {
        Dots dots = new Dots();

        for (String line : lineStrings) {
            String[] xy = line.split(",");

            dots.append(new Dot(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
        }

        return dots;
    }

    /**
     * 点到线段的距离是都小于指定值
     *
     * @param dots       线段点
     * @param dot        源点
     * @param deadLength 最小距离
     * @return 是否小于
     */
    public static boolean isDistanceLessThan(Dot[] dots, Dot dot, double deadLength) {
        for (int i = 0; i < dots.length - 1; i++) {
            Dot lineStartDot = dots[i];
            Dot lineEndDot = dots[i + 1];

            // 线段的距离
            double p1p2 = getLengthBetween2Pnt(lineStartDot, lineEndDot);
            // 点到线段一点的距离
            double p2p3 = getLengthBetween2Pnt(lineStartDot, dot);
            // 点到线段另一点的距离
            double p3p1 = getLengthBetween2Pnt(lineEndDot, dot);

            // 若点与线段形成钝角三角形，则判断点到线段端点的距离是否小于判断距离
            if (p2p3 * p2p3 + p1p2 * p1p2 < p3p1 * p3p1 || p3p1 * p3p1 + p1p2 * p1p2 < p2p3 * p2p3) {
                if (p2p3 <= deadLength || p2p3 <= deadLength) {
                    return true;
                }
            }

            // 非钝角三角形
            // 点到线段的距离
            double length = dotToLineLength(p1p2, p2p3, p3p1);

            if (length <= deadLength) {
                return true;
            }
        }

        return false;
    }

    /**
     * 以bottomLine为底，三角形的高
     *
     * @param bottomLine 三角形的底
     * @param sideLine1  三角形的边
     * @param sideLine2  三角形的另一边
     * @return 三角形的高
     */
    public static double dotToLineLength(double bottomLine, double sideLine1, double sideLine2) {
        double s = (bottomLine + sideLine1 + sideLine2) / 2;

        double area = Math.sqrt(s * (s - bottomLine) * (s - sideLine1) * (s - sideLine2));

        return area * 2 / bottomLine;
    }

    public static double getLengthBetween2Pnt(Dot dot1, Dot dot2) {
        return Math.sqrt(Math.pow(dot1.x - dot2.x, 2) + Math.pow(dot1.y - dot2.y, 2));
    }

    /**
     * 将获取计划原服务返回的字符串转换成列表数据
     */
    public static ArrayList<Hashtable<String, String>> dataToList(String str) {
        ArrayList<Hashtable<String, String>> data = new ArrayList<>();

        try {
            JsonFactory jsonFactory = new JsonFactory();

            if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1 && (str.charAt(1) == '[' || str.charAt(1) == '{')) {
                str = str.substring(1, str.length() - 1);
            }

            str = str.replace("\\", "");

            JsonParser jsonParser = jsonFactory.createJsonParser(str);
            jsonParser.nextToken();

            if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
                return data;
            }

            String name, text;
            JsonToken token;

            Hashtable<String, String> current = null;

            while (jsonParser.nextToken() != null) {

                token = jsonParser.getCurrentToken();
                name = jsonParser.getCurrentName();
                text = jsonParser.getText();

                switch (token) {
                    case START_OBJECT:
                        current = new Hashtable<>();

                        break;
                    case VALUE_NUMBER_INT:
                    case VALUE_STRING:
                        if (current != null)
                            current.put(name, text);

                        break;
                    case END_OBJECT:
                        data.add(current);
                        current = null;

                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
