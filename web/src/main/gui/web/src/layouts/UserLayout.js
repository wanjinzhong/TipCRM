import React from 'react';
import { Link, Redirect, Switch, Route } from 'dva/router';
import DocumentTitle from 'react-document-title';
import { Icon } from 'antd';
import GlobalFooter from '../components/GlobalFooter';
import styles from './UserLayout.less';
import logo from '../assets/logo.svg';
import { getRoutes } from '../utils/utils';

const links = [{
  key: 'help',
  title: '帮助',
  href: '',
}, {
  key: 'privacy',
  title: '隐私',
  href: '',
}, {
  key: 'terms',
  title: '条款',
  href: '',
}];

const copyright = <div>powered by 小窍门信息技术有限公司</div>;

/**
 * the user login and register page layout
 */
class UserLayout extends React.PureComponent {
  getPageTitle() {
    const { routerData, location } = this.props;
    const { pathname } = location;
    let title = 'Tip CRM';
    if (routerData[pathname] && routerData[pathname].name) {
      title = `${routerData[pathname].name} - Tip CRM`;
    }
    return title;
  }
  render() {
    const { routerData, match } = this.props;
    return (
      <DocumentTitle title={this.getPageTitle()}>
        <div className={styles.container}>
          <Switch>
            {getRoutes(match.path, routerData).map(item =>
              (
                <Route
                  key={item.key}
                  path={item.path}
                  component={item.component}
                  exact={item.exact}
                />
              )
            )}
            <Route key="test" path={'/user/test'} c/>
            <Redirect exact from="/user" to="/user/login" />
          </Switch>
          <GlobalFooter className={styles.footer} links={links} copyright={copyright}/>
        </div>
      </DocumentTitle>
    );
  }
}

export default UserLayout;
