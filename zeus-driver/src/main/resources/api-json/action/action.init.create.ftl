{
    "jsonrpc": "2.0",
    "method": "action.create",
    "params": {
        "name": "__offline_status__",
        "eventsource": 0,
        "status": 0,
        "esc_period": "2m",
        "filter": {
            "evaltype": 0,
            "conditions": [
                {
                    "conditiontype": 25,
                    "operator": 0,
                    "value": "DEVICE_STATUS"
                }
            ]
        },
        "operations": [
            {
                "operationtype": 1,
                "opcommand_grp": [
                    {
                        "groupid": "${groupId}"
                    }
                ],
                "opcommand": {
                    "scriptid": "${scriptId}"
                }
            }
        ]
    },
    "auth": "${userAuth}",
    "id": 1
}