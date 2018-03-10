package com.alipapa.smp.user.service;

import com.alipapa.smp.user.mapper.GroupMapper;
import com.alipapa.smp.user.pojo.Group;
import com.alipapa.smp.user.pojo.GroupExample;
import com.alipapa.smp.user.vo.GroupSelectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupMapper groupMapper;

    /**
     * @param id
     * @return
     */
    public Group getGroupById(Long id) {
        return groupMapper.selectByPrimaryKey(id);
    }

    /**
     * 获取组下拉列表
     *
     * @return
     */
    public List<GroupSelectVo> listAllGroupSelect() {
        GroupExample example = new GroupExample();

        List<Group> groupList = groupMapper.selectByExample(example);
        List<GroupSelectVo> groupSelectVoList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(groupList)) {

            for (Group group : groupList) {
                GroupSelectVo groupSelectVo = new GroupSelectVo();
                groupSelectVo.setGroupId(group.getId());
                groupSelectVo.setGroupNo(group.getGroupNo());
                groupSelectVo.setGroupName(group.getName());
                groupSelectVoList.add(groupSelectVo);
            }
        }
        return groupSelectVoList;
    }
}
