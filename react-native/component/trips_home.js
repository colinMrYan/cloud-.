import React, { Component } from 'react';
import {
  ActivityIndicator,
  ActivityIndicatorIOS,
  ProgressBarAndroid,
  Image,
  ListView,
  Platform,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import TimerEnhance from 'react-native-smart-timer-enhance'
import PullToRefreshListView from 'react-native-smart-pull-to-refresh-listview'


class TripsHome extends Component {

  constructor(props) {
    super(props);
    this.dataSource = new ListView.DataSource({
      rowHasChanged: (r1, r2) => r1 != r2,
    })
    let data = [
      {
        type: 'report',
      }
    ];
    this.state = {
      first: true,
      data,
      dataSource: this.dataSource.cloneWithRows(data),
    }
  }

  // 组件加载时自动刷新数据
  componentDidMount() {
    this._pullToRefreshListView.beginRefresh()
  }

  // 行渲染
  renderRow = (rowData, sectionId, rowId) => {
    return (
      <View style={styles.reportCard}>
        <Image style={{width: 44, height: 44, margin: 18}} source={require('./res/report.png')} />
        <Text style={{marginTop: 12, marginLeft: 2}}>
          <Text style={{color: '#444444', fontWeight: '600', fontSize: 17, lineHeight: 24}}>
            {`Q2费用报告 ${rowId}\n`}
          </Text>
          <Text style={{color: '#777777', fontSize: 14, lineHeight: 27}}>
            平台与技术部 2016-4-30
          </Text>
        </Text>
      </View>
    )
  }

  renderActivityIndicator() {
    return ActivityIndicator ? (
      <ActivityIndicator
        style={{marginRight: 10,}}
        animating={true}
        color={'#999'}
        size={'small'}/>
    ) : Platform.OS == 'android' ?
      (
        <ProgressBarAndroid
          style={{marginRight: 10,}}
          color={'#999'}
          styleAttr={'Small'}/>
      ) : (
      <ActivityIndicatorIOS
        style={{marginRight: 10,}}
        animating={true}
        color={'#999'}
        size={'small'}/>
      )
  }

  // 头部渲染
  renderHeader = (viewState) => {
    let {pullState, pullDistancePercent} = viewState
    let {refresh_none, refresh_idle, will_refresh, refreshing,} = PullToRefreshListView.constants.viewState
    pullDistancePercent = Math.round(pullDistancePercent * 100)
    switch(pullState) {
      case refresh_none:
      return (
        <View style={{height: 25, justifyContent: 'center', alignItems: 'center',}}>
          <Text style={{color: '#999'}}>下拉可以刷新</Text>
        </View>
      )
    case refresh_idle:
      return (
        <View style={{height: 25, justifyContent: 'center', alignItems: 'center',}}>
          <Text style={{color: '#999'}}>下拉可以刷新</Text>
        </View>
      )
    case will_refresh:
      return (
        <View style={{height: 25, justifyContent: 'center', alignItems: 'center',}}>
          <Text style={{color: '#999'}}>松开立即刷新</Text>
        </View>
      )
    case refreshing:
      return (
        <View style={{flexDirection: 'row', height: 25, justifyContent: 'center', alignItems: 'center',}}>
          {this.renderActivityIndicator()}<Text style={{color: '#999'}}>正在刷新数据...</Text>
        </View>
      )
    }
  }

  //
  onRefresh = () => {
    //console.log('outside _onRefresh start...')

    //simulate request data
    this.setTimeout( () => {

      //console.log('outside _onRefresh end...')
      let addNum = 20
      let refreshedDataList = []
      for(let i = 0; i < addNum; i++) {
          refreshedDataList.push({
              text: `item-${i}`
          })
      }

      this.setState({
          dataList: refreshedDataList,
          dataSource: this.dataSource.cloneWithRows(refreshedDataList),
      })
      this._pullToRefreshListView.endRefresh()

    }, 500)
}


  render() {
    return (
      <PullToRefreshListView
        style={{marginBottom: 10}}
        ref={ (component) => this._pullToRefreshListView = component }
        viewType={PullToRefreshListView.constants.viewType.listView}
        dataSource={this.state.dataSource}
        renderRow={this.renderRow}
        renderHeader={this.renderHeader}
        onRefresh={this.onRefresh}
        enabledPullUp={false}
      />
    )
    /*return (
      <View style={styles.reportCard}>
        <Image style={{width: 44, height: 44, margin: 18}} source={require('./res/report.png')} />
        <Text style={{marginTop: 12, marginLeft: 2}}>
          <Text style={{color: '#444444', fontWeight: '600', fontSize: 17, lineHeight: 24}}>
            {'Q2费用报告\n'}
          </Text>
          <Text style={{color: '#777777', fontSize: 14, lineHeight: 27}}>
            平台与技术部 2016-4-30
          </Text>
        </Text>
      </View>
    );*/
  }
}

export default TimerEnhance(TripsHome)

const styles = StyleSheet.create({
  reportCard: {
    marginTop: 11,
    height: 80,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 0.5,
    borderBottomColor: '#CDCDCD',
    borderTopWidth: 0.5,
    borderTopColor: '#CDCDCD',
    flexDirection: 'row',
  },
});
