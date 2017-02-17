import React, { Component } from 'react';
import {
  Image,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import QuickAccessButton from './quick_access_button';
import TripsHome from './trips_home';

export default class TestViewA extends Component {
  render() {
    return (
      <View style={styles.container}>
        <StatusBar
           backgroundColor="#0F7BCA"
           barStyle="light-content" />

        <View style={styles.navigationBar}>
          <Text style={styles.navigationBarTitle}>发现</Text>
        </View>

        <View style={styles.quickAccessCard}>
          <QuickAccessButton
            icon={require('./res/stastistics.png')}
            label="分析"
            target="ecc-component://stastistics"/>
          <QuickAccessButton
            icon={require('./res/news.png')}
            label="新闻"
            target="ecc-component://news.ecc"/>
          <QuickAccessButton
            icon={require('./res/knowledge.png')}
            label="知识"
            target="ecc-component://knowledge"/>
          <QuickAccessButton
            icon={require('./res/document.png')}
            label="文档"
            target="ecc-component://document"/>


        </View>

        <TripsHome></TripsHome>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F4F4F4',
  },

  navigationBar: {
    backgroundColor: '#0F7BCA',
    height: 64,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  navigationBarTitle: {
    height: 44,
    fontSize: 18,
    fontWeight: 'bold',
    color: '#FFFFFF',
    paddingTop: 12,
  },

  quickAccessCard: {
    height: 85,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 0.5,
    borderBottomColor: '#CDCDCD',
    flexDirection: 'row',
    justifyContent: 'space-around',
  },

});
