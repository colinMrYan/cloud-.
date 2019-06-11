import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  View
} from 'react-native';

export default class NavigationBar extends Component {
  render() {
    return (
      <Text style={styles.navigationBar}>
        发现
      </Text>
    );
  }
}

const styles = StyleSheet.create({
  navigationBar: {
    // flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#0F7BCA',
  },
  navigationBarTitle: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  }
});
