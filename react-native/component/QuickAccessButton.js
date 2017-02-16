import React, { Component } from 'react';
import {
  Image,
  Linking,
  StyleSheet,
  Text,
  TouchableOpacity,
  View
} from 'react-native';

export default class QuickAccessButton extends Component {
  pressHandler() {
    const url = this.props.target
    // alert(url);
    Linking.canOpenURL(url).then(supported => {
      if (!supported) {
        alert(`不支持的URL: ${url}`);
      } else {
        return Linking.openURL(url);
      }
    }).catch(err => {
      alert(`打开URL(${url})时发生异常！`);
    })
  }
  render() {
    return (
      <TouchableOpacity activeOpacity={0.5} onPress={this.pressHandler.bind(this)}>
        <View style={styles.quickAccessButton}>
          <Image style={styles.quickAccessIcon} source={this.props.icon} />
          <Text style={styles.quickAccessLabel}>{this.props.label}</Text>
        </View>
      </TouchableOpacity>
    );
  }
}

const styles = StyleSheet.create({

  quickAccessButton: {
    alignItems: 'center',
  },
  quickAccessIcon: {
    width: 44,
    height: 44,
    marginTop: 13,
  },
  quickAccessLabel: {
    fontSize: 12,
    color: '#666666',
    lineHeight: 17,
    marginTop: 2,
  },

});
