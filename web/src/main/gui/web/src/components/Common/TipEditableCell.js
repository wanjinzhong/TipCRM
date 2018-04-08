/**
 * Created by mosesc on 04/03/18.
 */
import React from 'react';
import {Input} from 'antd';

export default class TipEditableCell extends React.Component{
  render(){
    const {enableEdit, editing, value, createNew, handleSaveValue, handleValueChange, style} = this.props;
    if (createNew){
      return (<Input defaultValue={value} onPressEnter={handleSaveValue} onChange={handleValueChange}/>);
    }
    const editPanel = editing ? <Input defaultValue={value} onPressEnter={handleSaveValue} onChange={handleValueChange}/> : value;
    return(<div>
      {enableEdit ? editPanel: value}
    </div>);
  }
}
