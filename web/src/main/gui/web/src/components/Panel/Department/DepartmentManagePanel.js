/**
 * Created by mosesc on 04/02/18.
 */
import React from 'react';
import {connect} from 'dva';
import {Button} from 'antd';
import {getPermission} from '../../../utils/PermissionConstant';
import CommonSpin from '../../Common/CommonSpin';
import SearchTable from '../../Common/SearchTable';
import TipEditableCell from '../../Common/TipEditableCell';
import RoleOperationCell from '../RoleAndPermission/RoleOperationCell';
import TipEditableSelectCell from '../../Common/TipEditableSelectCell';
import styles from './Index.css';

@connect(({loading, permission, department, user}) =>({
  loadingPermission:loading.models.permission,
  menuPermissions: permission.menuPermissions,
  loading: loading.models.department,
  departments: department.departments,
  users: user.users,
}))
export default class DepartmentManagePanel extends React.Component{
  state={
    createNew: false,
    selectDepartment: {},
    newDepartmentName: null,
  }
  componentDidMount(){
    const {dispatch, menuId} = this.props;
    dispatch({
      type: 'permission/getPermissionsByMenu',
      payload: {menuId: menuId}
    });
    dispatch({
      type: 'department/listDepartments',
    });
  }

  handleDepartmentAdd(){
    let {dispatch, departments} = this.props;
    let newDepartment = {id: '--', editing: true, createNew: true};
    departments.push(newDepartment);
    dispatch({
      type: 'department/saveDepartments',
      payload: departments,
    });
    this.setState({
      createNew: true,
    });
  }

  handleSaveDepartment(e, item, field){

    // console.log(value    // const value = e.target.value;);
    const {dispatch} = this.props;
    if (item.id){
      dispatch({
        type: 'department/updateDepartment',

      });
    } else {
      dispatch({
        type: 'department/createNewDepartment',
        payload: {name: this.state.newDepartmentName}
      });
    }
  }
  handleDepartmentEdit(record){
    const {dispatch, departments} = this.props;
    console.log(departments);
    if (record.editing){

    } else {
      let newDepartments = departments.filter(item => item.id != record.id);
      record.editing = true;
      newDepartments.push(record);
      dispatch({
        type: 'department/saveDepartments',
        payload: newDepartments,
      })
    }
  }
  handleDepartmentDelete(record){
    const {dispatch, departments} = this.props;
    dispatch({
      type: 'role/deleteRole',
      payload: {deleteId: record.id, departments: departments}
    });
  }

  handleDepartmentNameChange(e){
    this.setState({
      newDepartmentName: e.target.value,
    });
  }

  handleSelectCellOnChange(value){

  }
  handleSelectCellOnSearch(value){
    console.log(">>>>>>>>>>>>>>>"+value);
    const {dispatch} = this.props;
    dispatch({
      type: 'user/getUserByName',
      payload: {userName: value, includeDismiss: false},
    })
  }

  render(){
    const {menuPermissions, departments, loading, loadingPermission, users} = this.props;
    // init permissions
    const {permissions} = menuPermissions;
    const enableAdd = permissions.filter(item => item === getPermission('DEPARTMENT_ADD')).length > 0;
    const enableEdit = permissions.filter(item => item === getPermission('DEPARTMENT_EDIT')).length > 0;
    const enableDelete = permissions.filter(item => item === getPermission('DEPARTMENT_DELETE')).length > 0;
    const data = [{id: '1', name:'李白'}]

    let columns = [
      {title: '部门编号', dataIndex:'id', width: '10%'},
      {title: '部门名称', dataIndex:'name', width: '15%', render:((text, item, index) =>
        <TipEditableCell editing={item.editing}
                          enableEdit={enableEdit} value={item.name}
                          createNew = {item.createNew}
                          handleValueChange = {this.handleDepartmentNameChange.bind(this)}
                          handleSaveValue = {this.handleSaveDepartment.bind(this, item, 'name')}/>)},
      {title: '部门经理', dataIndex:'manager.name', width: '15%', render:((text, item, index) =>
        <TipEditableSelectCell editing={item.editing} enableEdit={enableEdit} data={users}
                          selectData={item.manager ? item.manager:''} createNew = {item.createNew} fetching={true}
                          handleOnSearch = {this.handleSelectCellOnSearch.bind(this, item, 'manager.name')}
                               handleValueChange = {this.handleSelectCellOnChange.bind(this)}/>)},
      {title: '部门人数', width: '10%', dataIndex: 'total'},
      {title: '创建人', width: '20%', dataIndex:'entryUser'},
      {title: '创建时间', width: '20%', dataIndex:'entryTime'},
    ];
    if (enableEdit || enableDelete){
      const column = {title: '操作', render:(record =>
        <RoleOperationCell
          showEdit={enableEdit}
          showDelete={enableDelete}
          handleEditClick = {this.handleDepartmentEdit.bind(this, record)}
          handleDeleteClick={this.handleDepartmentDelete.bind(this, record)} />)};
      columns.push(column);
    }

    return(<div>
      <CommonSpin spinning={loading}>
      <SearchTable
        tableClass = {styles.tableClass}
        tableColumns={columns}
        tableData={departments}
        tableRowKey = "id"
        tablePagination={false}/>
        <CommonSpin spinning={loadingPermission}>{
          enableAdd ? <Button
            style={{ width: '60%', marginTop: 8, marginBottom: 16, marginLeft:'20%' }}
            type="dashed"
            onClick={this.handleDepartmentAdd.bind(this)}
            icon="plus">
            新增部门
          </Button> : <div></div>
        }
      </CommonSpin>
    </CommonSpin></div>);
  }
}
