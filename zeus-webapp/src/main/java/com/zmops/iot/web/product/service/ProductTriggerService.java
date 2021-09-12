package com.zmops.iot.web.product.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.zmops.iot.domain.product.ProductStatusFunction;
import com.zmops.iot.domain.product.ProductStatusFunctionRelation;
import com.zmops.iot.domain.product.query.QProductStatusFunction;
import com.zmops.iot.domain.product.query.QProductStatusFunctionRelation;
import com.zmops.iot.web.product.dto.ProductStatusFunctionDto;
import com.zmops.iot.web.product.dto.ProductStatusJudgeRule;
import com.zmops.zeus.driver.service.ZbxDeviceStatusTrigger;
import io.ebean.DB;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author nantian created at 2021/8/10 17:55
 * <p>
 * <p>
 * 创建 设备 状态触发器
 */

@Service
public class ProductTriggerService {


    @Autowired
    private ZbxDeviceStatusTrigger deviceStatusTrigger;

    /**
     * 离线 或者 在线触发器 信息
     *
     * @param relationId 在线规则
     * @return ResponseData
     */
    public ProductStatusFunctionDto getRule(String relationId) {
        String sql = "select s.*,p.name attrName,p2.name attrNameRecovery,p.units,p2.units unitsRecovery " +
                " from product_status_function s LEFT JOIN product_attribute p on p.attr_id = s.attr_id LEFT JOIN product_attribute p2 on p2.attr_id = s.attr_id_recovery \n" +
                " where s.rule_id in (select rule_id from product_status_function_relation where relation_id = :relationId)";
        return  DB.findDto(ProductStatusFunctionDto.class, sql).setParameter("relationId", relationId).findOne();
    }


    /**
     * 创建 设备状态 触发器
     *
     * @param judgeRule 判断规则
     * @return Integer
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDeviceStatusJudgeTrigger(ProductStatusJudgeRule judgeRule) {

        long ruleId = IdUtil.getSnowflake().nextId();
        judgeRule.setRuleId(ruleId);
//        Map<String, String> rule = new HashMap<>();
//        buildTriggerCreateMap(rule, judgeRule);

        String res = deviceStatusTrigger.createDeviceStatusTrigger(judgeRule.getRuleId() + "", judgeRule.getRelationId(),
                judgeRule.getProductAttrKey(), judgeRule.getRuleCondition() + judgeRule.getUnit(), judgeRule.getRuleFunction(), judgeRule.getProductAttrKeyRecovery(),
                judgeRule.getRuleConditionRecovery() + judgeRule.getUnitRecovery(), judgeRule.getRuleFunctionRecovery());

        String triggerId = getTriggerId(res);

        ProductStatusFunction productStatusFunction = new ProductStatusFunction();
        BeanUtils.copyProperties(judgeRule, productStatusFunction);
        productStatusFunction.setZbxId(triggerId);
        productStatusFunction.setRuleId(ruleId);
        DB.save(productStatusFunction);

        ProductStatusFunctionRelation productStatusFunctionRelation = new ProductStatusFunctionRelation();
        productStatusFunctionRelation.setRelationId(judgeRule.getRelationId());
        productStatusFunctionRelation.setRuleId(ruleId);
        DB.save(productStatusFunctionRelation);

        return productStatusFunction.getRuleId();
    }

    /**
     * 修改 设备状态 触发器
     *
     * @param judgeRule 判断规则
     * @return Integer
     */
    public Long updateDeviceStatusJudgeTrigger(ProductStatusJudgeRule judgeRule) {
//        Map<String, String> rule = new HashMap<>();
//        buildTriggerCreateMap(rule, judgeRule);

        deviceStatusTrigger.updateDeviceStatusTrigger(judgeRule.getTriggerId(), judgeRule.getRuleId() + "", judgeRule.getRelationId(),
                judgeRule.getProductAttrKey(), judgeRule.getRuleCondition() + judgeRule.getUnit(), judgeRule.getRuleFunction(), judgeRule.getProductAttrKeyRecovery(),
                judgeRule.getRuleConditionRecovery() + judgeRule.getUnitRecovery(), judgeRule.getRuleFunctionRecovery());

        ProductStatusFunction productStatusFunction = new ProductStatusFunction();
        BeanUtils.copyProperties(judgeRule, productStatusFunction);
        DB.update(productStatusFunction);

        return judgeRule.getRuleId();
    }

    /**
     * 值拷贝
     *
     * @param rule      新规则
     * @param judgeRule 规则
     */
    private void buildTriggerCreateMap(Map<String, String> rule, ProductStatusJudgeRule judgeRule) {
        rule.put("ruleId", judgeRule.getRuleId() + "");
        rule.put("deviceId", judgeRule.getRelationId());

        rule.put("ruleFunction", judgeRule.getRuleFunction());
        rule.put("ruleCondition", judgeRule.getRuleCondition());
        rule.put("itemKey", judgeRule.getProductAttrKey());

        rule.put("itemKeyRecovery", judgeRule.getProductAttrKeyRecovery());
        rule.put("ruleConditionRecovery", judgeRule.getRuleConditionRecovery());
        rule.put("ruleFunctionRecovery", judgeRule.getRuleFunctionRecovery());
    }


    private String getTriggerId(String responseStr) {
        TriggerIds ids = JSON.parseObject(responseStr, TriggerIds.class);
        if (null != ids && ids.getTriggerids().length > 0) {
            return ids.getTriggerids()[0];
        }
        return null;
    }

    /**
     * 删除 离线 或者 在线触发器
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceStatusTrigger(Long ruleId) {
        new QProductStatusFunctionRelation().ruleId.eq(ruleId).delete();

        new QProductStatusFunction().ruleId.eq(ruleId).delete();
    }


    @Data
    static class TriggerIds {
        String[] triggerids;
    }
}
