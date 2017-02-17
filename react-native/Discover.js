/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  StyleSheet,
  Navigator,
} from 'react-native';
import DiscoverHome from './component/discover_home';

export default class Discover extends Component {
  render() {
    const defaultName = 'DiscoverHome';
    const defaultComponent = DiscoverHome;

    return (
      <Navigator
        style={styles.navigator}
        initialRoute={{ name: defaultName, component: defaultComponent }}
        /*configureScene={(route) => {
          return Navigator.SceneConfigs.HorizontalSwipeJump;
        }}*/
        renderScene={(route, navigator) => {
          let Component = route.component;
          return <Component {...route.params} navigator={navigator} />
        }} />
    );
  }
}

const styles = StyleSheet.create({
  navigator: {
    backgroundColor: '#333',
  },
});
